package sevice

import cats.effect.MonadCancelThrow
import cats.implicits.{catsSyntaxApplyOps, toFunctorOps}
import cats.syntax.applicativeError.*
import cats.syntax.either.*
import cats.syntax.flatMap.*
import dao.ArticleSql
import domain.Author
import domain.errors.{AppError, *}
import doobie.*
import doobie.implicits.*
import org.typelevel.log4cats.Logger
import telegramium.bots.ChatIntId

trait ArticleStorage[F[_]] {

  def addAuthor(chatId: ChatIntId, author: Author): F[Either[AppError, Unit]]

  def deleteAuthor(chatId: ChatIntId, author: Author): F[Either[AppError, Unit]]

  def getListAuthors(chatId: ChatIntId): F[Either[AppError, List[String]]]

}

object ArticleStorage {

  private final class Impl[F[_]: MonadCancelThrow](
    articleSql: ArticleSql,
    transactor: Transactor[F]
  ) extends ArticleStorage[F] {

    private def updateAuthor[Err <: AppError](
      io: ConnectionIO[Either[Err, Unit]]
    ): F[Either[AppError, Unit]] =
      io.transact(transactor)
        .attempt
        .map(_.leftMap(InternalError.apply))
        .map {
          case Left(error)          => Left(error)
          case Right(Left(error))   => Left(error)
          case Right(Right(result)) => Right(result)
        }

    override def addAuthor(chatId: ChatIntId, author: Author): F[Either[AppError, Unit]] =
      updateAuthor(articleSql.addAuthor(chatId, author))

    override def deleteAuthor(chatId: ChatIntId, author: Author): F[Either[AppError, Unit]] =
      updateAuthor(articleSql.deleteAuthor(chatId, author))

    override def getListAuthors(chatId: ChatIntId): F[Either[AppError, List[String]]] =
      articleSql
        .getListAuthors(chatId)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError.apply))

  }

  private final class LoggingImpl[F[_]: MonadCancelThrow: Logger](storage: ArticleStorage[F])
      extends ArticleStorage[F] {

    private def surroundWithLogs[Error, Res](
      io: F[Either[Error, Res]]
    )(
      inputLog: String
    )(
      errorOutputLog: Error => (String, Option[Throwable]),
      successOutputLog: Res => String
    ): F[Either[Error, Res]] =
      Logger[F].info(inputLog) *> io.flatTap {
        case Left(error) =>
          val (logString: String, throwable: Option[Throwable]) = errorOutputLog(error)
          Logger[F].error(logString)
        case Right(result) => Logger[F].info(successOutputLog(result))
      }

    override def addAuthor(chatId: ChatIntId, author: Author): F[Either[AppError, Unit]] =
      surroundWithLogs(storage.addAuthor(chatId, author))("adding author")(
        err => (s"${err.message} ${err.cause}", err.cause),
        _ => "add author successfully"
      )

    override def deleteAuthor(chatId: ChatIntId, author: Author): F[Either[AppError, Unit]] =
      surroundWithLogs(storage.deleteAuthor(chatId, author))("deleting author")(
        err => (s"${err.message} ${err.cause}", err.cause),
        _ => "delete author successfully"
      )

    override def getListAuthors(chatId: ChatIntId): F[Either[AppError, List[String]]] =
      surroundWithLogs(storage.getListAuthors(chatId))("get list of autthors")(
        err => (s"${err.message} ${err.cause}", err.cause),
        _ => "success"
      )

  }

  def make[F[_]: MonadCancelThrow: Logger](
    articleSql: ArticleSql,
    transactor: Transactor[F]
  ): ArticleStorage[F] = {
    val impl = new Impl(articleSql, transactor)
    new LoggingImpl[F](impl)
  }

}
