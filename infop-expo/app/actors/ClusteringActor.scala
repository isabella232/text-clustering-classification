package actors

import akka.actor.Actor.Receive
import akka.actor.{Props, Actor, ActorRef}

/**
  * Created by sayantamd on 29/2/16.
  */

object ClusteringActor {
  def props(out: ActorRef) = Props(new ClusteringActor(out))
}


class ClusteringActor(out: ActorRef) extends Actor {

  override def receive: Receive = {
    case dayValue: String =>
      out ! "output.json"
  }
}
