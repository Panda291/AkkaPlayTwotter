package controllers

import models.{Global, LoginAttempt, RegisterAttempt, UserDao}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.Inject

class UserController @Inject()(
    cc: MessagesControllerComponents,
    userDao: UserDao
) extends MessagesAbstractController(cc) {

    private val logger = play.api.Logger(this.getClass)

    val loginForm: Form[LoginAttempt] = Form (
        mapping(
            "username" -> nonEmptyText
                .verifying("too few chars",  s => lengthIsGreaterThanNCharacters(s, 2))
                .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 20)),
            "password" -> nonEmptyText
                .verifying("too few chars",  s => lengthIsGreaterThanNCharacters(s, 2))
                .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 30)),
        )(LoginAttempt.apply)(LoginAttempt.unapply)
    )

    private val formSubmitUrl = routes.UserController.processLoginAttempt

    def showLoginForm: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
        Ok(views.html.userLogin(loginForm, formSubmitUrl))
    }

    def processLoginAttempt: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
        val errorFunction = { formWithErrors: Form[LoginAttempt] =>
            // form validation/binding failed...
            BadRequest(views.html.userLogin(formWithErrors, formSubmitUrl))
        }
        val successFunction = { user: LoginAttempt =>
            // form validation/binding succeeded ...
            val foundUser: Boolean = userDao.lookupUser(user)
            if (foundUser) {
                Redirect(routes.HomeController.index())
                    .flashing("info" -> "You are logged in.")
                    .withSession(Global.SESSION_USERNAME_KEY -> user.username)
            } else {
                Redirect(routes.UserController.showLoginForm())
                    .flashing("error" -> "Invalid username/password.")
            }
        }
        val formValidationResult: Form[LoginAttempt] = loginForm.bindFromRequest
        formValidationResult.fold(
            errorFunction,
            successFunction
        )
    }

  val registerForm: Form[RegisterAttempt] = Form (
    mapping(
      "username" -> nonEmptyText
        .verifying("too few chars",  s => lengthIsGreaterThanNCharacters(s, 2))
        .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 20)),
      "password" -> nonEmptyText
        .verifying("too few chars",  s => lengthIsGreaterThanNCharacters(s, 2))
        .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 30)),
      "verifyPassword" -> nonEmptyText
        .verifying("too few chars",  s => lengthIsGreaterThanNCharacters(s, 2))
        .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 30)),
    )(RegisterAttempt.apply)(RegisterAttempt.unapply).verifying("Passwords do not match", s => s.password == s.verifyPassword)
  )

    private val registerFormSubmitUrl = routes.UserController.processRegisterAttempt

    def showRegisterForm: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
        Ok(views.html.register(registerForm, registerFormSubmitUrl))
    }

    def processRegisterAttempt: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
      val errorFunction = { formWithErrors: Form[RegisterAttempt] =>
        BadRequest(views.html.register(formWithErrors, registerFormSubmitUrl))
      }
      val successFunction = { user: RegisterAttempt =>
        val addedUser: Boolean = userDao.addUser(user.toLoginAttempt)
        if(addedUser) {
            Redirect(routes.HomeController.index())
              .flashing("info" -> "Account created successfully.")
              .withSession(Global.SESSION_USERNAME_KEY -> user.username)
        } else {
            Redirect(routes.UserController.showRegisterForm())
              .flashing("error" -> "User already Exists.")
        }
      }
      val formValidationResult: Form[RegisterAttempt] = registerForm.bindFromRequest
      formValidationResult.fold(
          errorFunction,
          successFunction
      )
    }

    def userList: Action[AnyContent] = Action { implicit request =>
      Ok(views.html.userNames(userDao))
    }

    private def lengthIsGreaterThanNCharacters(s: String, n: Int): Boolean = {
        if (s.length > n) true else false
    }

    private def lengthIsLessThanNCharacters(s: String, n: Int): Boolean = {
        if (s.length < n) true else false
    }

}
