package models

import akka.http.scaladsl.model.DateTime

case class NewCommentAttempt(content: String, target: Int) {
  def toComment(username: String): Comment = {
    Comment(username, content, DateTime.now)
  }
}
