package services

import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import models._
import play.api.Configuration
import play.api.libs.json.Reads
import play.api.libs.ws._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

@Singleton
class DribbbleStatsService @Inject()(wsClient: WSClient, config: Configuration, actorSystem: ActorSystem) {

  private lazy val serviceEndpoint = config.get[String]("api.dribbble.endpoint")
  private lazy val serviceAccessToken = config.get[String]("api.dribbble.clientAccessToken")

  import models.JsonProtocol._

  def getLikers(login: String): Future[List[(User, Int)]] = {
    getFollowers(login) flatMap { followers =>
      Future.sequence {
        followers.filter(_.follower.shots_count > 0) map (follower => getShots(follower.follower.username))
      }
    } flatMap { shots =>
      Future.sequence {
        shots.flatten.filter(_.likes_count > 0) map (shot => getLikes(shot))
      }
    } map { likes =>
      likes.flatten.groupBy(_.user.id) mapValues (userLikes => (userLikes.head.user, userLikes.length))
    } map (_.values.toList.sortBy(-_._2))
  }

  def getLikes(shot: Shot): Future[List[Like]] = {
    request[List[Like]](s"/shots/${shot.id}/likes")
  }

  def getShots(username: String): Future[List[Shot]] = {
    request[List[Shot]](s"/users/$username/shots")
  }

  def getFollowers(username: String): Future[List[Follower]] = {
    request[List[Follower]](s"/users/$username/followers")
  }

  private[this] def request[T](serviceUrl: String)(implicit reader: Reads[T]): Future[T] = {
    wsClient.url(s"$serviceEndpoint/$serviceUrl").withHttpHeaders("Authorization" -> s"Bearer $serviceAccessToken")
      .get() flatMap { response =>
      response.status match {
        case 200 => Try {
          response.json.as[T]
        } match {
          case Success(results) => Future.successful(results)
          case Failure(e) => Future.failed(e)
        }
        case 429 => response.header("X-RateLimit-Reset").map(_.toLong * 1000) match {
          case None => Future.failed(new Exception(s"Number of retries exceeded: ${response.status} ${response.body}"))
          case Some(timestamp) =>
            delay(FiniteDuration(timestamp - new Date().getTime, TimeUnit.MILLISECONDS)) {
              request[T](serviceUrl)
            }
        }
        case _ => Future.failed(new Exception(s"Invalid response: ${response.status} ${response.body}"))
      }
    }
  }

  private[this] def delay[T](delay: FiniteDuration)(action: => Future[T]) = {
    val promise = Promise[T]()

    actorSystem.scheduler.scheduleOnce(delay) {
      promise.completeWith(action)
    }(actorSystem.dispatcher)

    promise.future
  }
}

