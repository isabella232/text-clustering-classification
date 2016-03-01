package domain

import play.api.libs.json._

/**
  * Created by sayantamd on 1/3/16.
  */

object ClusterResponse {

  def withData(data: String, messageType: MessageType = MessageType.HeartBeat): ClusterResponse = {
    val cr = ClusterResponse(messageType)
    cr.data = data
    cr
  }

  def withException(ex: Exception): ClusterResponse = {
    val cr = ClusterResponse(MessageType.ClusterFail)
    cr.data = ex.getMessage
    cr
  }

  val clusterResponseReads = new Reads[ClusterResponse] {
    override def reads(json: JsValue): JsResult[ClusterResponse] = {
      val jsObj = json.asInstanceOf[JsObject]
      val cr: ClusterResponse = new ClusterResponse(
        messageType = MessageType.valueOf((json \ "messageType").validate[String].get))
      if (jsObj.keys.contains("data")) {
        cr.data = (jsObj \ "data").validate[String].get
      }
      JsSuccess(value = cr)
    }
  }

  val clusterResponseWrites = new Writes[ClusterResponse] {
    override def writes(o: ClusterResponse): JsValue = Json.obj(
      "messageType" -> o.messageType.toString,
      "data" -> o.data
    )
  }
}

case class ClusterResponse(messageType: MessageType) {
  var data: String = null
}
