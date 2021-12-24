package models

import akka.http.scaladsl.model.DateTime

case class Tweet(
                  id: Long,
                  username: String,
                  content: String,
                  hashtag: String,
                  var likes: List[String],
                  var comments: List[Comment],
                  timestamp: DateTime,
                  image: Option[String] = None,
                  var sharedBy: Option[String] = None,
                )
