package controllers

import models.{NewTweetAttempt, TweetDao}
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

import java.nio.file.Paths
import javax.inject._

@Singleton
class TweetController @Inject()(
                                 cc: MessagesControllerComponents,
                                 authenticatedUserAction: AuthenticatedUserAction,
                                 tweetDao: TweetDao
                               ) extends MessagesAbstractController(cc) {

  val newTweetForm: Form[NewTweetAttempt] = Form(
    mapping(
      "content" -> nonEmptyText
        .verifying("too few chars",  s => lengthIsGreaterThanNCharacters(s, 2))
        .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 160)),
      "hashtag" -> text
        .verifying("no spaces in #", s => !s.contains(" "))
        .verifying("hashtag should start with #", s => s == "" || s.head == '#')
        .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 20)),
    )(NewTweetAttempt.apply)(NewTweetAttempt.unapply)
  )

  private val formSubmitUrl = routes.TweetController.postTweet

  def showTimeLine(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isDefined) {
      Ok(views.html.TimeLine(
        tweetDao.tweetsSortedByDate(),
        newTweetForm,
        formSubmitUrl
      ))
    } else {
      Redirect(routes.UserController.showLoginForm())
    }
  }

  def postTweet = Action(parse.multipartFormData) { implicit request =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isDefined) {
      var imageLink: Option[String] = None
      request.body
        .file("picture")
        .map { picture =>
          val filename = Paths.get(picture.filename).getFileName
          imageLink = Some(filename.toUri.toString.split('/').last)
          val fileSize = picture.fileSize
          val contentType = picture.contentType

          picture.ref.copyTo(Paths.get(s"./public/images/$filename"), replace = true)
          Ok("File uploaded")
        }
        .getOrElse {
          BadRequest(views.html.TimeLine(tweetDao.tweetsSortedByDate(), newTweetForm, formSubmitUrl))
        }
      val errorFunction = { formWithErrors: Form[NewTweetAttempt] =>
        BadRequest(views.html.TimeLine(tweetDao.tweetsSortedByDate(), formWithErrors, formSubmitUrl))
      }
      val successFunction = { tweet: NewTweetAttempt =>
        tweetDao.addTweet(
          request.session.get(models.Global.SESSION_USERNAME_KEY).get,
          tweet.content,
          tweet.hashtag,
          imageLink
        )
        Redirect(routes.TweetController.showTimeLine())
          .flashing("info" -> "Tweet posted successfully.")
      }
      val formValidationResult: Form[NewTweetAttempt] = newTweetForm.bindFromRequest
      formValidationResult.fold(
        errorFunction,
        successFunction
      )
    } else {
      Redirect(routes.UserController.showLoginForm()) // debug if i'm not logged in
    }
  }

  private def lengthIsGreaterThanNCharacters(s: String, n: Int): Boolean = {
    if (s.length > n) true else false
  }

  private def lengthIsLessThanNCharacters(s: String, n: Int): Boolean = {
    if (s.length < n) true else false
  }

}

