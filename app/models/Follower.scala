package models

case class Follower(
  id: Long,
  follower: User
)

//object Follower {
//  implicit val format: OFormat[Follower] = Json.format[Follower]
//}