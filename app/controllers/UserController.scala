package controllers

import javax.inject.Inject
import models.{Global, LoginAttempt, User, UserDao}
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

class UserController @Inject()(
    cc: MessagesControllerComponents,
    userDao: UserDao
) extends MessagesAbstractController(cc) {

    private val logger = play.api.Logger(this.getClass)

    val form: Form[LoginAttempt] = Form (
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
        Ok(views.html.userLogin(form, formSubmitUrl))
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
        val formValidationResult: Form[LoginAttempt] = form.bindFromRequest
        formValidationResult.fold(
            errorFunction,
            successFunction
        )
    }

    private def lengthIsGreaterThanNCharacters(s: String, n: Int): Boolean = {
        if (s.length > n) true else false
    }

    private def lengthIsLessThanNCharacters(s: String, n: Int): Boolean = {
        if (s.length < n) true else false
    }

}
