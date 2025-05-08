package dao

import domain.Author
import domain.errors.*
import doobie.*
import doobie.implicits.*
import telegramium.bots.ChatIntId

trait ArticleSql {

  def addAuthor(
    chatId: ChatIntId,
    author: Author
  ): ConnectionIO[Either[UserAlreadySubscribedToAuthor, Unit]]

  def deleteAuthor(
    chatId: ChatIntId,
    author: Author
  ): ConnectionIO[Either[UserNotSubscribedToAuthor, Unit]]

  def getListAuthors(chatId: ChatIntId): ConnectionIO[List[String]]

}

object ArticleSql {

  private object sqls {

    def addAuthorSql(chatId: ChatIntId, author: Author): Query0[Int] =
      sql"""
        select * from user_authors.add_author_f(${chatId.id}, ${author.authorTag})
         """.query[Int]

    def deleteAuthorSql(chatId: ChatIntId, author: Author): Query0[Int] =
      sql"""
           select * from user_authors.delete_author_f(${chatId.id}, ${author.authorTag})
         """.query[Int]

    def getListAuthorsSql(chatId: ChatIntId): Query0[String] =
      sql"""
           select * from user_authors.get_list_authors_f(${chatId.id})
         """.query[String]

  }

  private final class Impl extends ArticleSql {

    import sqls.*

    override def addAuthor(
      chatId: ChatIntId,
      author: Author
    ): ConnectionIO[Either[UserAlreadySubscribedToAuthor, Unit]] =
      addAuthorSql(chatId, author).option.map {
        case Some(count) if count != 0 => Right(())
        case _                         => Left(new UserAlreadySubscribedToAuthor)
      }

    override def deleteAuthor(
      chatId: ChatIntId,
      author: Author
    ): ConnectionIO[Either[UserNotSubscribedToAuthor, Unit]] =
      deleteAuthorSql(chatId, author).option.map {
        case Some(count) if count != 0 => Right(())
        case _                         => Left(new UserNotSubscribedToAuthor)
      }

    override def getListAuthors(chatId: ChatIntId): ConnectionIO[List[String]] =
      getListAuthorsSql(chatId).to[List]

  }

  def make: ArticleSql = new Impl

}
