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
          tweetDao.tweetsById(user.sharedTweets).map({ tweet =>
            tweet.copy(sharedBy = Some(username))
          })
        Ok(views.html.profilePage(user, tweets.sortBy(_.timestamp).reverse, routes.TweetController.removeTweet,
          userDao.getUser(request.session.get(models.Global.SESSION_USERNAME_KEY).get)))
      } else {
        NoContent
      }
    } else {
      Redirect(routes.UserController.showLoginForm())
    }
  }

  def followUser(username: String) = Action { implicit request: MessagesRequest[AnyContent] =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isDefined) {
      val clientUser = userDao.getUser(request.session.get(models.Global.SESSION_USERNAME_KEY).get)
      val targetUser = userDao.getUser(username)
      if (clientUser == targetUser) { // user can not follow themselves
        NoContent
      } else {
        if (userDao.followUser(clientUser, targetUser)) {
          Ok("followed")
        } else {
          Ok("unfollowed")
        }
      }
    } else {
      Unauthorized
    }
  }
}
