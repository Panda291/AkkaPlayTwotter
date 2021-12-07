package models

import akka.http.scaladsl.model.DateTime

case class Comment(username: String, content: String, likes: Int, timestamp: DateTime)
