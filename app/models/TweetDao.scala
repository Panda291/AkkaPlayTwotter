package models

import akka.http.scaladsl.model.DateTime

import scala.collection.mutable.ListBuffer
import javax.inject.{Inject, Singleton}

@Singleton
class TweetDao @Inject()(userDao: UserDao) {
  var tweets: ListBuffer[Tweet] = ListBuffer(
    Tweet(1, "admin", "lorem ipsum", "#MyFirstTestUwU", 8, List[Comment](
      Comment("user", "lorem ipsum", 3, DateTime.now),
      Comment("user2", "lorem ipsum 2", 5, DateTime.now)
    ), DateTime.now),
    Tweet(2, "user2", "second tweet", "#test", 8, List[Comment](
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
      val tweet = Tweet(nextIndex, username, content, hashtag, 0, List(), DateTime.now, imageLink)
      tweets += tweet
  }

  def removeTweetById(id: Int): Unit = {
    tweets = tweets.filter(_.id != id)
    userDao.removeSharedTweetById(id)
  }

  def tweetsSortedByDate(): List[Tweet] = {
    tweets.toList.sortWith(_.timestamp > _.timestamp)
  }
}
