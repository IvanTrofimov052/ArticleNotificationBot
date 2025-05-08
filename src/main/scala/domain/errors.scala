package domain

import cats.syntax.option.*

object errors {

  sealed abstract class AppError(val message: String, val cause: Option[Throwable] = None)

  case class InternalError(cause0: Throwable) extends AppError("Internal error", cause0.some)

  case class JsonParserError(override val message: String) extends AppError(message = message)

  case class UserNotSubscribedToAuthor() extends AppError("user not subscribed to author")

  case class UserAlreadySubscribedToAuthor() extends AppError("user already subscribed to author")

}
