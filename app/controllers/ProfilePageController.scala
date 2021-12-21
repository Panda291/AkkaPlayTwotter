package controllers

import models.{Tweet, TweetDao, UserDao}
import play.api.mvc._

import javax.inject.Inject

class ProfilePageController @Inject()(userDao: UserDao,
                                      tweetDao: TweetDao,
                                      cc: MessagesControllerComponents,
                                      authenticatedUserAction: AuthenticatedUserAction,
                                     ) extends MessagesAbstractController(cc) {
  def showProfilePage(username: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isDefined) {
      if (userDao.lookupUser(username)) {
        val user = userDao.getUser(username)
        val tweets: List[Tweet] = tweetDao.tweetsOfUser(username) ++
          tweetDao.tweetsById(user.sharedTweets).map({tweet =>
            tweet.sharedBy = Some(username)
          tweet
          })
        Ok(views.html.profilePage(user, tweets.sortBy(_.timestamp).reverse, routes.TweetController.removeTweet))
      } else {
        NoContent
      }
    } else {
      Redirect(routes.UserController.showLoginForm())
    }
  }
}
