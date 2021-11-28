package controllers
import controllers.{AuthenticatedUserAction, routes}
import javax.inject._
import models.ContactDao
import play.api.mvc._
@Singleton
class ContactPageController @Inject()(
                                       cc: ControllerComponents,
                                       authenticatedUserAction: AuthenticatedUserAction
                                     ) extends AbstractController(cc) {


  def listContacts = authenticatedUserAction { implicit request: Request[AnyContent] =>

    val contacts = ContactDao.findAll
    Ok(views.html.contacts(contacts))
  }

}
