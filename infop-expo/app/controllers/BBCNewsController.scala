package controllers

import java.io.File

import actors.ClusteringActor
import akka.actor.ActorRef
import domain.ClusterCommand
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter
import play.api.mvc._

import play.api.Play.current

class BBCNewsController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.bbcnews.index(request))
  }

  implicit val inEventFormat = Json.format[ClusterCommand]
  implicit val inEventFrameFormatter = FrameFormatter.jsonFrame[ClusterCommand]

  def createCluster = WebSocket.acceptWithActor[ClusterCommand, String] { request =>
    val config = play.api.Play.current.configuration
    (out: ActorRef) => ClusteringActor.props(out, config)
  }

  def clusterOutput(clusterId: String) = Action {
    val config = play.api.Play.current.configuration
    Ok.sendFile(
      content = new File(config.getString("app.cluster.outputPath").get, clusterId),
      inline = true
    )
  }
}
