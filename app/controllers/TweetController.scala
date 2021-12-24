package controllers

import models.{NewCommentAttempt, NewTweetAttempt, RemoveTweetAttempt, TweetDao, UserDao}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.JsValue
import play.api.mvc._

import java.nio.file.Paths
import javax.inject._

@Singleton
class TweetController @Inject()(
                                 cc: MessagesControllerComponents,
                                 authenticatedUserAction: AuthenticatedUserAction,
                                 tweetDao: TweetDao,
                                 userDao: UserDao
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
  private val formRemoveUrl = routes.TweetController.removeTweet

  def showTimeLine(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isDefined) {
      val clientUser = userDao.getUser(request.session.get(models.Global.SESSION_USERNAME_KEY).get)
      var tweets = tweetDao.tweetsForUserSortedByDate(clientUser)
      for (followedUser <- clientUser.followedUsers) {
        tweets = tweets ++ tweetDao.tweetsSharedByUser(userDao.getUser(followedUser)).map({tweet =>
          tweet.copy(sharedBy = Some(followedUser))
        })
      }

      Ok(views.html.TimeLine(
//        tweetDao.tweetsSortedByDate(),
        tweets.sortBy(_.timestamp).reverse,
        newTweetForm,
        formSubmitUrl,
        formRemoveUrl,
        clientUser
      ))
    } else {
      Redirect(routes.UserController.showLoginForm())
    }
  }

  def postTweet = Action(parse.multipartFormData) { implicit request =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isDefined) {
//      https://www.playframework.com/documentation/2.8.x/ScalaFileUpload
      var imageLink: Option[String] = None
      request.body
        .file("picture")
        .map { picture =>
          val filename = Paths.get(picture.filename).getFileName
          imageLink = Some(filename.toUri.toString.split('/').last)

          picture.ref.copyTo(Paths.get(s"./public/images/$filename"), replace = true)
          Ok("File uploaded")
        }
        .getOrElse {
          BadRequest(views.html.TimeLine(tweetDao.tweetsSortedByDate(), newTweetForm, formSubmitUrl, formRemoveUrl,
            userDao.getUser(request.session.get(models.Global.SESSION_USERNAME_KEY).get)))
        }
      // end of part found online
      val errorFunction = { formWithErrors: Form[NewTweetAttempt] =>
        BadRequest(views.html.TimeLine(tweetDao.tweetsSortedByDate(), formWithErrors, formSubmitUrl, formRemoveUrl,
          userDao.getUser(request.session.get(models.Global.SESSION_USERNAME_KEY).get)))
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

  // TODO: this form should not be necessary at all (see likeTweet)
  val removeTweetForm: Form[RemoveTweetAttempt] = Form(
    mapping(
      "id" -> number
        .verifying("Tweet not found", s => tweetDao.tweetExists(s))
    )(RemoveTweetAttempt.apply)(RemoveTweetAttempt.unapply)
  )

  def removeTweet = Action { implicit request =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isDefined) {
      val errorFunction = { formWithErrors: Form[RemoveTweetAttempt] =>
        BadRequest(views.html.TimeLine(tweetDao.tweetsSortedByDate(), newTweetForm, formSubmitUrl, formRemoveUrl,
          userDao.getUser(request.session.get(models.Global.SESSION_USERNAME_KEY).get)))
      }
      val successFunction = { tweet: RemoveTweetAttempt =>
        if (tweetDao.removeTweet(tweet.id, request.session.get(models.Global.SESSION_USERNAME_KEY).get)) {
//          Redirect(request.headers.apply("Referer")) // redirect to caller of this function (useful for profile page)
          Ok
        } else {
          NoContent
        }
      }
      val formValidationResult: Form[RemoveTweetAttempt] = removeTweetForm.bindFromRequest
      formValidationResult.fold(
        errorFunction,
        successFunction
      )
    } else Redirect(routes.UserController.showLoginForm())
  }

  def likeTweet = authenticatedUserAction { implicit request =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isDefined) {
      val body: Option[Map[String, Seq[String]]] = request.body.asFormUrlEncoded
      body.map { body =>
        if (tweetDao.likeTweet(body("id").head.toInt, request.session.get(models.Global.SESSION_USERNAME_KEY).get)) {
          Ok("added")
        } else {
          Ok("removed")
        }
      }.getOrElse {
        BadRequest("Expecting application/json request body")
      }
    } else {
      Unauthorized
    }
  }


  val newCommentForm: Form[NewCommentAttempt] = Form(
    mapping(
      "content" -> nonEmptyText
        .verifying("too few chars",  s => lengthIsGreaterThanNCharacters(s, 2))
        .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 160)),
      "target" -> number
    )(NewCommentAttempt.apply)(NewCommentAttempt.unapply)
  )

  def commentOnTweet = Action { implicit request =>
    val errorFunction = { formWithErrors: Form[NewCommentAttempt] =>
      BadRequest
    }
    val successFunction = { commentAttempt: NewCommentAttempt =>
      val comment = commentAttempt.toComment(request.session.get(models.Global.SESSION_USERNAME_KEY).get)
      tweetDao.addComment(commentAttempt.target, comment)
      Ok
    }
    val formValidationResult: Form[NewCommentAttempt] = newCommentForm.bindFromRequest
    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }

  def shareTweet = Action { implicit request =>
    if (request.session.get(models.Global.SESSION_USERNAME_KEY).isDefined) {
      val body: Option[Map[String, Seq[String]]] = request.body.asFormUrlEncoded
      body.map { body =>
        if (request.session.get(models.Global.SESSION_USERNAME_KEY).get != tweetDao.getTweetById(body("id").head.toLong).username) {
          if (userDao.shareTweet(request.session.get(models.Global.SESSION_USERNAME_KEY).get, tweetDao.getTweetById(body("id").head.toLong))) {
            Ok("shared")
          } else {
            Ok("unshared")
          }
        } else {
          Unauthorized
        }
      }.getOrElse {
        BadRequest("Expecting application/json request body")
      }
    } else {
      Unauthorized
    }
  }

  private def lengthIsGreaterThanNCharacters(s: String, n: Int): Boolean = {
    if (s.length > n) true else false
  }

  private def lengthIsLessThanNCharacters(s: String, n: Int): Boolean = {
    if (s.length < n) true else false
  }

}

