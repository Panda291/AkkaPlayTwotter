package models

import javax.inject.Inject
import scala.collection.mutable

@javax.inject.Singleton
class UserDao @Inject()() {
  var users: mutable.Map[String, User] = mutable.Map[String, User](
    "user" -> User("user", "user", List(1)),
    "user1" -> User("user1", "user1", List(2)),
    "user2" -> User("user2", "user2", List()),
    "admin" -> User("admin", "admin", List())
  )

  def lookupUser(u: LoginAttempt): Boolean = {
    if (users.contains(u.username)) true else false
  }

  def removeSharedTweetById(id: Int): Unit = {
    for ((_, user) <- users) {
      user.sharedTweets = user.sharedTweets.filter(_ != id)
    }
  }
}