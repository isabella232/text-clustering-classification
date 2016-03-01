package domain

import play.api.libs.json._

import scala.collection.mutable

/**
  * Created by sayantamd on 1/3/16.
  */

object ClusterResponse {

  def withData(data: Map[String, String], messageType: MessageType = MessageType.HeartBeat): ClusterResponse = {
    val cr = ClusterResponse(messageType)
    cr.data ++= data
    cr
  }

  def withException(ex: Exception): ClusterResponse = {
    val cr = ClusterResponse(MessageType.ClusterFail)
    cr.data += (("exception", ex.getMessage))
    cr
  }

  val clusterResponseReads = new Reads[ClusterResponse] {
    override def reads(json: JsValue): JsResult[ClusterResponse] = {
      val jsObj = json.asInstanceOf[JsObject]
      val cr: ClusterResponse = new ClusterResponse(
        messageType = MessageType.valueOf((json \ "messageType").validate[String].get))
      if (jsObj.keys.contains("data")) {
        cr.data ++= (jsObj \ "data").validate[Map[String, String]].get
      }
      JsSuccess(value = cr)
    }
  }

  val clusterResponseWrites = new Writes[ClusterResponse] {
    override def writes(o: ClusterResponse): JsValue = Json.obj(
      "messageType" -> o.messageType.toString,
      "data" -> o.data.foldLeft(Json.obj()) { case (z, (k, v)) => z + (k, JsString(v)) }
    )
  }
}

case class ClusterResponse(messageType: MessageType) {
  var data = mutable.Map[String, String]()
}
