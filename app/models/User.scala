package models

case class User(
                 username: String,
                 password: String,
                 var sharedTweets: List[Long], // list of tweets shared by this user
                 var followedUsers: List[String] = List()
               )

