package controllers

import models.TweetDao
import play.api.mvc._

import javax.inject._
@Singleton
class LandingPageController @Inject()(
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction,
    tweetDao: TweetDao
) extends AbstractController(cc) {

    // this is where the user comes immediately after logging in.
    // notice that this uses `authenticatedUserAction`.
    def showLandingPage() = authenticatedUserAction { implicit request: Request[AnyContent] =>
        Ok(views.html.loginLandingPage(
            tweetDao.tweets.toList
        ))
    }

}

