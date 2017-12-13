package controllers

import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future
import services.DribbbleStatsService
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class AppController @Inject()(dribbbleStatsService: DribbbleStatsService, cc: ControllerComponents) extends AbstractController(cc) {
  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */

  import models.JsonProtocol._

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def top10(login: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    login match {
      case None => Future.successful {
        BadRequest(Json.obj("message" -> "Login parameter is missing"))
      }
      case Some(username) =>
        dribbbleStatsService.getLikers(username) map { data => Ok(Json.toJson(data.take(10)))
        }
    }
  }
}
