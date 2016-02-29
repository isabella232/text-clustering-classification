package controllers

import java.io.File

import actors.ClusteringActor
import akka.actor.ActorRef
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc._

import play.api.Play.current

class BBCNewsController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.bbcnews.index(request))
  }

  def createCluster = WebSocket.acceptWithActor[String, String] { request =>
    (out: ActorRef) => ClusteringActor.props(out)
  }

  def clusterOutput(clusterId: String) = Action {

    val config = play.api.Play.current.configuration
    Ok.sendFile(
      content = new File(config.getString("app.cluster.outputPath").get.concat("/%s").format(clusterId)),
      inline = true
    )
  }
}
