package models

import play.api.libs.json._

object JsonProtocol {

  implicit val userWrites: OWrites[User] = Json.writes[User]
  implicit val userReads: Reads[User] = Json.reads[User]

  implicit val followerWrites: Writes[Follower] = Json.writes[Follower]
  implicit val followerReads: Reads[Follower] = Json.reads[Follower]
//  implicit val followerFormat: OFormat[Follower] = Json.format[Follower]

  implicit val shotWrites: OWrites[Shot] = Json.writes[Shot]
  implicit val shotReads: Reads[Shot] = Json.reads[Shot]

  implicit val likeWrites: OWrites[Like] = Json.writes[Like]
  implicit val likeReads: Reads[Like] = Json.reads[Like]

  implicit val statsWrites: Writes[(User, Int)] = Writes[(User, Int)] { entry =>
    Json.obj(
      "user" -> entry._1,
      "count"  -> entry._2
    )
  }

}