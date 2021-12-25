package models

import akka.http.scaladsl.model.DateTime

import scala.collection.mutable.ListBuffer
import javax.inject.{Inject, Singleton}

@Singleton
class TweetDao @Inject()(userDao: UserDao) {
  var tweets: ListBuffer[Tweet] = ListBuffer(
    Tweet(1, "admin", "lorem ipsum", "#MyFirstTestUwU", List(), List[Comment](
      Comment("user", "lorem ipsum", DateTime.now),
      Comment("user2", "lorem ipsum 2", DateTime.now)
    ), DateTime.now, Some("780739435678400542.gif")),
    Tweet(2, "user2", "second tweet", "#test", List(), List[Comment](
      Comment("user3", "only one comment for this one", DateTime.now)
    ), DateTime.now)
  )
  var nextIndex: Long = tweets.map(_.id).max + 1

  def tweetsOfUser(username: String): List[Tweet] = {
    tweets.filter(_.username == username).toList
  }

  def tweetsById(ids: List[Long]): List[Tweet] = {
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
    tweets.toList.sortBy(_.timestamp).reverse
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

  def addComment(id: Long, comment: Comment): Unit = {
    val tweet = getTweetById(id)
    tweet.comments = (comment :: tweet.comments).sortBy(_.timestamp).reverse
  }

  def tweetsForUserSortedByDate(user: User): List[Tweet] = {
    tweets.filter({tweet =>
      user.followedUsers.contains(tweet.username) // all tweets of which the creator is followed by the client
    }).toList
  }

  def tweetsSharedByUser(user: User): List[Tweet] = {
    tweets.filter({tweet =>
      user.sharedTweets.contains(tweet.id)
    }).toList
  }

  def getTweetsByHashtag(hashtag: String): List[Tweet] = {
    tweets.filter(_.hashtag == hashtag).toList
  }
}
