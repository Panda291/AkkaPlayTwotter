package models

import javax.inject.Inject
import scala.collection.mutable

@javax.inject.Singleton
class UserDao @Inject()() {
  var users: mutable.Map[String, User] = mutable.Map[String, User](
    "user" -> User("user", "user", List(1), List("admin")),
    "user1" -> User("user1", "user1", List(2)),
    "user2" -> User("user2", "user2", List()),
    "admin" -> User("admin", "admin", List())
  )

  def lookupUser(u: LoginAttempt): Boolean = {
    if (users.contains(u.username)) true else false
  }

  def lookupUser(username: String): Boolean = {
    if (users.contains(username)) true else false
  }

  def removeSharedTweetById(id: Int): Unit = {
    for ((_, user) <- users) {
      user.sharedTweets = user.sharedTweets.filter(_ != id)
    }
  }

  def addUser(u: LoginAttempt): Boolean = {
    if (!lookupUser(u)) {
      users += (u.username -> User(u.username, u.password, List()))
      true
    } else false
  }

  def getUser(u: String): User = {
    users(u)
  }

  def followUser(clientUser: User, targetUser: User): Boolean = { //true if now following, else false (method follows and unfollows)
    if (clientUser.followedUsers.contains(targetUser.username)) {
      clientUser.followedUsers = clientUser.followedUsers.filter(_ != targetUser.username)
      false
    } else {
      clientUser.followedUsers = targetUser.username :: clientUser.followedUsers
      true
    }
  }

  def shareTweet(username: String, tweet: Tweet): Boolean = { //true if now shared, else false (method shares and unshares)
    val user = getUser(username)
    if (user.sharedTweets.contains(tweet.id)) {
      user.sharedTweets = user.sharedTweets.filter(_ != tweet.id)
      false
    } else {
      user.sharedTweets = tweet.id :: user.sharedTweets
      true
    }
  }

  def allUsernames(): Seq[String] = {
    users.keys.toSeq
  }
}