package models

case class User (
    username: String,
    password: String,
    var sharedTweets: List[Int] // list of tweets shared by this user
)

