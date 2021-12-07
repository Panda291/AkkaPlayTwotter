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

  def tweetsOfUser(username: String): List[Tweet] = {
    tweets.filter(_.username == username).toList
  }

  def tweetsById(ids: List[Int]): List[Tweet] = {
    tweets.filter(ids.contains(_)).toList
  }

  def addTweet(tweet: Tweet): Unit = {
    tweets += tweet
  }

  def removeTweetById(id: Int): Unit = {
    tweets = tweets.filter(_.id != id)
    userDao.removeSharedTweetById(id)
  }
}
