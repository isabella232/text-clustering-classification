package actors

import java.io.File
import java.util.UUID

import akka.actor.{Props, Actor, ActorRef}
import akka.util.Timeout
import domain.{ClusterResponse, MessageType, PersonalitiesClusterCommand, SparkCommand}
import play.api.Configuration
import util.SparkFacade

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Created by sayantamd on 2/3/16.
  */

object PersonalitiesClusteringActor {
  def props(out: ActorRef, config: Configuration): Props = Props(new PersonalitiesClusteringActor(out, config))
}

class PersonalitiesClusteringActor(out: ActorRef, config: Configuration) extends Actor with SparkAware {
  override def receive: Receive = {
    case command: PersonalitiesClusterCommand =>

      val inputPath = config.getString("app.cluster.personalities.inputPath").get

      val outputDirPath = new File(config.getString("app.cluster.outputPath").get)
      val outputFileName: String = "%s.json".format(UUID.randomUUID())
      val outputFilePath = new File(outputDirPath, outputFileName).getAbsolutePath

      out ! ClusterResponse(MessageType.ClusterStart)

      import play.api.libs.concurrent.Execution.Implicits._
      val future = Future {
        SparkFacade.invokeSpark(
          config.getConfig("app.cluster.spark").get,
          SparkCommand(inputPath, outputFilePath)
        )
      }

      while (!future.isCompleted) {
        Thread.sleep(1000)
        out ! ClusterResponse(MessageType.HeartBeat)
      }

      try {
        import scala.language.postfixOps
        Await.result(future, Timeout(1 second).duration)
        val data = Map(
          "clusterId" -> outputFileName
        )
        out ! ClusterResponse.withData(data, MessageType.ClusterSuccess)
      } catch {
        case ex: Exception => out ! ClusterResponse.withException(ex)
      }

  }
}
