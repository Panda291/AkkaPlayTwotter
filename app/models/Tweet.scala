package models

import akka.http.scaladsl.model.DateTime

case class Tweet(
                  id: Int,
                  username: String,
                  content: String,
                  hashtag: String,
                  likes: Int,
                  comments: List[Comment],
                  timestamp: DateTime
                )
