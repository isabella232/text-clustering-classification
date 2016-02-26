package com.tpg.infop.domain

import java.io.{FileWriter, File}
import java.net.URL

import akka.actor._
import akka.event.Logging
import akka.routing.SmallestMailboxPool
import com.tpg.infop.domain.FeedItemActor.FetchFeedItem
import com.typesafe.config.ConfigFactory
import org.apache.commons.codec.digest.DigestUtils
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.util.parsing.json.JSONObject

/**
  * Created by sayantamd on 12/11/15.
  */

object FeedItemActor {
  case class FetchFeedItem(url: String)
}

class FeedItemActor extends Actor {

  import FeedItemActor._

  val log = Logging(context.system, this)

  override def receive: Actor.Receive = {
    case FetchFeedItem(url) =>
      val digest = DigestUtils.sha1Hex(url)
      val dumpDirPath = ConfigFactory.load().getConfig("app.infop").getString("feedItemDirectory")
      val dumpDir = new File(dumpDirPath)
      var absoluteDumpDir: File = null
      if (!dumpDir.isAbsolute) {
        absoluteDumpDir = new File(new File(System.getProperty("user.home")), dumpDir.getPath)
      } else {
        absoluteDumpDir = dumpDir
      }

      val file = new File("%s/%s.txt".format(absoluteDumpDir, digest))
      if (!file.exists()) {
        log.debug("Fetching story with URL: {}", url)
        val doc = Jsoup.connect(url).get()
        val text = doc.body().text()
        val w = new FileWriter(file)
        w.write(String.format("%s||||%s", url, text))
        w.flush()
        w.close()
      }
  }
}

object AnalysisActor {
  case object Analyze
  var router: ActorRef = null
}

class AnalysisActor extends Actor {
  import AnalysisActor._
  import com.rometools.rome.io.{XmlReader, SyndFeedInput}

  val log = Logging(context.system, this)

  override def preStart() = {
    if (router == null) {
      router = context.actorOf(SmallestMailboxPool(10).props(Props[FeedItemActor]), "feedItemRouter")
    }
  }

  override def receive: Receive = {
    case Analyze =>
      log.debug("AnalysisActor invoked")
      SubscriptionsHelper.all.par.foreach(feed => {
        val feedItems = parseFeed(feed.url)
        feedItems.foreach(item => router ! FetchFeedItem(item))
      })
      log.debug("AnalysisActor finished")
  }

  def parseFeed(feedUrl: String): List[String] = {
    log.debug("Fetching feed with URL: {}", feedUrl)
    val input = new SyndFeedInput()
    val feed = input.build(new XmlReader(new URL(feedUrl)))
    feed.getEntries.asScala.toList.map(e => e.getLink)
  }
}

object AnalysisRun {
  import AnalysisActor._

  var analyzer: ActorRef = null

  def invokeAnalysis(system: ActorSystem) = {

    if (analyzer == null) {
      analyzer = system.actorOf(Props[AnalysisActor], "analyzer")
    }

    analyzer ! Analyze
  }
}
