package models

import akka.http.scaladsl.model.DateTime

import scala.collection.mutable.ListBuffer
import javax.inject.{Inject, Singleton}

@Singleton
class TweetDao @Inject()(userDao: UserDao) {
  var tweets: ListBuffer[Tweet] = ListBuffer(
    Tweet(1, "admin", "lorem ipsum", "#MyFirstTestUwU", List(), List[Comment](
      Comment("user", "lorem ipsum", 3, DateTime.now),
      Comment("user2", "lorem ipsum 2", 5, DateTime.now)
    ), DateTime.now),
    Tweet(2, "user2", "second tweet", "#test", List(), List[Comment](
      Comment("user3", "only one comment for this one", 555555, DateTime.now)
    ), DateTime.now)
  )
  var nextIndex: Long = tweets.map(_.id).max + 1

  def tweetsOfUser(username: String): List[Tweet] = {
    tweets.filter(_.username == username).toList
  }

  def tweetsById(ids: List[Int]): List[Tweet] = {
    tweets.filter(t => {
      ids.contains(t.id)
    }).toList
  }

  def addTweet(username: String, content: String, hashtag: String, imageLink: Option[String]): Unit = {
      val tweet = Tweet(nextIndex, username, content, hashtag, List(), List(), DateTime.now, imageLink)
      nextIndex += 1
      tweets += tweet
  }

  def removeTweetById(id: Int): Unit = {
    tweets = tweets.filter(_.id != id)
    userDao.removeSharedTweetById(id)
  }

  def tweetsSortedByDate(): List[Tweet] = {
    tweets.toList.sortWith(_.timestamp > _.timestamp)
  }

  def tweetExists(id: Int): Boolean = {
    tweets.exists(_.id == id)
  }

  def removeTweet(id: Long, username: String): Boolean = {
    try {
      val tweet = tweets.filter(_.id == id).head
      if (tweet.username == username) {
        tweets -= tweet
        true
      } else false
    } catch {
      case _: Exception => false
    }
  }

  def getTweetById(id: Long): Tweet = {
    tweets.filter(_.id == id).head
  }

  def likeTweet(id: Long, username: String): Boolean = { // true if tweet is now liked, else false (method also unlikes if already likes)
    try {
      val tweet = getTweetById(id)
      if (!tweet.likes.contains(username)) {
        tweet.likes = username :: tweet.likes
        true
      } else {
        tweet.likes = tweet.likes.filter(_ != username)
        false
      }
    } catch {
      case e: NoSuchElementException => false
    }
  }

//  def unlikeTweet(id: Long, username: String): Unit = {
//    val tweet = getTweetById(id)
//    tweet.likes = tweet.likes.filter(_ != username)
//  }
}
