package controllers

import models.UserDao
import play.api.mvc._

import javax.inject.Inject

class DebugController @Inject()(userDao: UserDao, cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  def showUserListPage: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.debugUserListPage(userDao.users.values.toList))
  }
}
