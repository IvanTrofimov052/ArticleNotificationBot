package controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import domain.Author
import domain.errors.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sevice.ArticleStorage
import telegramium.bots.ChatIntId

class MockStorageWithoutErrors extends ArticleStorage[IO] {

  override def getListAuthors(chatId: ChatIntId): IO[Either[AppError, List[String]]] =
    IO.pure(Right(List("A", "B", "C")))

  override def addAuthor(chatId: ChatIntId, author: Author): IO[Either[AppError, Unit]] =
    IO.pure(Right(()))

  override def deleteAuthor(chatId: ChatIntId, author: Author): IO[Either[AppError, Unit]] =
    IO.pure(Right(()))

}

class MockStorageWithErrors extends ArticleStorage[IO] {

  override def getListAuthors(chatId: ChatIntId): IO[Either[AppError, List[String]]] =
    IO.pure(Left(InternalError(new Throwable("problem with connect to db"))))

  override def addAuthor(chatId: ChatIntId, author: Author): IO[Either[AppError, Unit]] =
    IO.pure(Left(UserNotSubscribedToAuthor()))

  override def deleteAuthor(chatId: ChatIntId, author: Author): IO[Either[AppError, Unit]] =
    IO.pure(Left(InternalError(new Throwable("problem with connect to db"))))

}

class CommandControllerSpec extends AnyFlatSpec with Matchers {

  val storageWithoutErrors = new MockStorageWithoutErrors
  val storageWithErrors    = new MockStorageWithErrors

  "CommandController.addAuthorCommand" should "work correctly with command /add" in {
    CommandController
      .make[IO](storageWithoutErrors)
      .addAuthorCommand(domain.Author("AUTHOR"), ChatIntId(155))
      .unsafeRunSync() shouldBe
      "Successfully added author"
  }

  it should "return error if its cant add author to user" in {
    CommandController
      .make[IO](storageWithErrors)
      .addAuthorCommand(domain.Author("AUTHOR"), ChatIntId(155))
      .unsafeRunSync() shouldBe
      "Something went wrong on Server user not subscribed to author"
  }

  "CommandController.deleteAuthorCommand" should "work correctly with command /delete" in {
    CommandController
      .make[IO](storageWithoutErrors)
      .deleteAuthorCommand(domain.Author("AUTHOR"), ChatIntId(155))
      .unsafeRunSync() shouldBe
      "Successfully delete author"
  }

  it should "return error if its cant delete author from user" in {
    CommandController
      .make[IO](storageWithErrors)
      .deleteAuthorCommand(domain.Author("a"), ChatIntId(155))
      .unsafeRunSync() shouldBe
      "Something went wrong on Server Internal error"
  }

  "CommandController.getListAuthorsCommand" should "work correctly with command /list" in {
    CommandController
      .make[IO](storageWithoutErrors)
      .getListAuthorsCommand(ChatIntId(155))
      .unsafeRunSync() shouldBe
      "Your authors:\nA\nB\nC"
  }

  "CommandController.getListAuthorsCommand" should "return error if its cant get list" in {
    CommandController
      .make[IO](storageWithErrors)
      .getListAuthorsCommand(ChatIntId(155))
      .unsafeRunSync() shouldBe
      "Something went wrong on Server Internal error"
  }

}
