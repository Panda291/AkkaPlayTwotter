package controllers

import models.{TweetDao, UserDao}
import play.api.mvc._

import javax.inject.Inject

class ProfilePageController @Inject()(userDao: UserDao,
                                      tweetDao: TweetDao,
                                      cc: ControllerComponents,
                                      authenticatedUserAction: AuthenticatedUserAction,
                                     ) extends AbstractController(cc) {
  def showProfilePage(username: String): Action[AnyContent] = authenticatedUserAction { implicit request: Request[AnyContent] =>
    if (userDao.lookupUser(username)) {
      val user = userDao.getUser(username)
      val tweets = tweetDao.tweetsOfUser(username) ++ tweetDao.tweetsById(user.sharedTweets)
      Ok(views.html.profilePage(user, tweets))
    } else {
      NoContent
    }
  }
}
