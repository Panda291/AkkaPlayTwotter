package models

case class Tweet(
                  username: String,
                  userTag: String,
                  content: String,
                  hashtag: String,
                  likes: Int,
                  comments: List[Comment]
                )
