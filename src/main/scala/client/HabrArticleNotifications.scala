package client

import cats.ApplicativeThrow
import cats.syntax.functor.*
import config.ArticleNotificationsConfig
import domain.errors
import domain.errors.*
import io.circe.parser.*
import sttp.client4.*

private class ArticleNotificationsImpl[F[_]: ApplicativeThrow](
  config: ArticleNotificationsConfig,
  backend: Backend[F]
) extends ArticleNotificationClient[F] {

  override def getArticleNotifications: F[Either[AppError, List[ArticleNotification]]] =
    basicRequest.get(uri"${config.url}").send(backend).map(_.body).map {
      case Left(error) => Left(JsonParserError(error))
      case Right(value) =>
        parse(value) match {
          case Left(error) => Left(JsonParserError(error.message))
          case Right(json) =>
            json.hcursor.downField("result").as[List[ArticleNotification]] match
              case Left(error)  => Left(JsonParserError(error.message))
              case Right(value) => Right(value)
        }
    }

}

object HabrArticleNotifications {

  def apply[F[_]: ApplicativeThrow](
    config: ArticleNotificationsConfig,
    backend: Backend[F]
  ): ArticleNotificationClient[F] =
    new ArticleNotificationsImpl[F](config, backend)

}
