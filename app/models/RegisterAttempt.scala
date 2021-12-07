package models

case class RegisterAttempt(username: String, password: String, verifyPassword: String) {
  def toLoginAttempt: LoginAttempt = {
    LoginAttempt(username, password)
  }
}
