package controllers

import com.tpg.infop.domain._
import play.api.libs.json._
import play.api.mvc.Results.EmptyContent
import play.api.mvc._

/**
  * Created by sayantamd on 7/11/15.
  */
class Subscriptions extends Controller {

  implicit val subscriptionReads = new Reads[Subscription] {
    override def reads(json: JsValue): JsResult[Subscription] = {
      val s: Subscription = new Subscription(id = -1, url = (json \ "url").validate[String].get)
      JsSuccess(value = s)
    }
  }

  implicit val subscriptionWrites = new Writes[Subscription] {
    override def writes(o: Subscription): JsValue = Json.obj(
      "id" -> o.id.toString,
      "url" -> o.url
    )
  }

  def index = Action {
    Ok(Json.toJson(SubscriptionsHelper.all))
  }

  def create = Action(parse.json) {
    (feed: Request[JsValue]) =>

      val feedResult = feed.body.validate[Subscription]
      feedResult match {
        case s: JsSuccess[Subscription] =>
          SubscriptionsHelper.create(s.get)
          Ok(EmptyContent())

        case e: JsError => BadRequest(EmptyContent())
      }
  }

}
