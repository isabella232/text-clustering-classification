package com.tpg.infop.domain

import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

/**
  * Created by sayantamd on 8/11/15.
  */
case class Subscription(id: Long, url: String)

class SubscriptionTable(tag: Tag) extends Table[Subscription](tag, "subscriptions") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def url = column[String]("url")
  def * = (id, url) <> (Subscription.tupled, Subscription.unapply _)
}

object SubscriptionsHelper {

  def all: List[Subscription] = {

    val subscriptionList = mutable.MutableList[Subscription]()
    val db = Database.forConfig("information_palace")
    try {
      val subscriptions = TableQuery[SubscriptionTable]
      val q = subscriptions.take(10000L)
      val action = q.result
      val future = db.run(action)
      future.onComplete {
        case Success(feeds) => for (feed <- feeds) {
          subscriptionList.+=(feed)
        }
        case Failure(t) => println("An error has occurred: " + t.getMessage)
      }
      Await.ready(future, Duration.Inf)

    } finally db.close()

    return subscriptionList.toList
  }

  def create(subscription: Subscription) = {

    val db = Database.forConfig("information_palace")
    try {
      val subscriptions = TableQuery[SubscriptionTable]
      val future = db.run(subscriptions += subscription)
      Await.ready(future, Duration.Inf)
    } finally db.close()
  }
}
