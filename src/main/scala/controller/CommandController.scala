package controller

import cats.Applicative
import cats.implicits.toFunctorOps
import domain.Author
import domain.errors.AppError
import sevice.ArticleStorage
import telegramium.bots.ChatIntId

trait CommandController[F[_]: Applicative] {

  def addAuthorCommand(author: Author, chatId: ChatIntId): F[String]

  def deleteAuthorCommand(author: Author, chatId: ChatIntId): F[String]

  def getListAuthorsCommand(chatId: ChatIntId): F[String]

  def helpCommand: F[String]

  def startCommand: F[String]

}

object CommandController {

  private final class Impl[F[_]: Applicative](storage: ArticleStorage[F])
      extends CommandController[F]() {

    override def addAuthorCommand(author: Author, chatId: ChatIntId): F[String] =
      storage.addAuthor(chatId, author).map {
        case Left(error) => s"Something went wrong on Server ${error.message}"
        case Right(list) => "Successfully added author"
      }

    override def deleteAuthorCommand(author: Author, chatId: ChatIntId): F[String] =
      storage.deleteAuthor(chatId, author).map {
        case Left(error) => s"Something went wrong on Server ${error.message}"
        case Right(list) => "Successfully delete author"
      }

    override def startCommand: F[String] = Applicative[F].pure("""Hello!
        |use /help""".stripMargin)

    override def helpCommand: F[String] = Applicative[F].pure("""
      to add channel use /add {habr user name}
        |to delete channel use /delete {habr user name}
        |to get list of subscribed authors use /list
      """.stripMargin)

    override def getListAuthorsCommand(chatId: ChatIntId): F[String] =
      storage.getListAuthors(chatId).map {
        case Left(error) => s"Something went wrong on Server ${error.message}"
        case Right(list) => list.fold("Your authors:")((result, el) => s"$result\n$el")
      }

  }

  def make[F[_]: Applicative](storage: ArticleStorage[F]): CommandController[F] =
    new Impl[F](storage)

}
