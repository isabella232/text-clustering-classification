package controllers

import java.io.File

import actors.PersonalitiesClusteringActor
import akka.actor.ActorRef
import domain.{ClusterResponse, PersonalitiesClusterCommand}
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter
import play.api.mvc._

/**
  * Created by sayantamd on 2/3/16.
  */
class PersonalitiesController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.personalities.index(request))
  }

  implicit val inEventFormat = Json.format[PersonalitiesClusterCommand]

  implicit val clusterResponseReads = ClusterResponse.clusterResponseReads
  implicit val clusterResponseWrites = ClusterResponse.clusterResponseWrites

  implicit val inEventFrameFormatter = FrameFormatter.jsonFrame[PersonalitiesClusterCommand]
  implicit val outEventFrameFormatter = FrameFormatter.jsonFrame[ClusterResponse]

  def createCluster = WebSocket.acceptWithActor[PersonalitiesClusterCommand, ClusterResponse] { request =>
    val config = play.api.Play.current.configuration
    (out: ActorRef) => PersonalitiesClusteringActor.props(out, config)
  }

  def clusterOutput(clusterId: String) = Action {
    val config = play.api.Play.current.configuration
    Ok.sendFile(
      content = new File(config.getString("app.cluster.outputPath").get, clusterId),
      inline = true
    )
  }

  def clusterNode(file:String) = Action {
    val config = play.api.Play.current.configuration
    Ok.sendFile(
      content = new File(config.getString("app.cluster.personalities.inputPath").get, s"/$file"),
      inline = true
    )
  }
}
