package bot

import cats.Parallel
import cats.effect.Async
import cats.implicits.*
import cats.syntax.all.toFunctorOps
import client.ArticleNotification
import controller.CommandController
import domain.Author
import telegramium.bots.high.implicits.*
import telegramium.bots.high.{Api, LongPollBot, Methods}
import telegramium.bots.{ChatIntId, Message}

class ArticleTelegramBot[F[_]: Async: Parallel](commandController: CommandController[F])(implicit
  api: Api[F]
) extends LongPollBot[F](api) {

  override def onMessage(message: Message): F[Unit] = message.text match {
    case Some(text) =>
      commandControl(text, ChatIntId(message.chat.id))
        .flatMap(messageText =>
          Methods.sendMessage(chatId = ChatIntId(message.chat.id), text = messageText).exec
        )
        .void
    case _ =>
      Methods
        .sendMessage(
          chatId = ChatIntId(message.chat.id),
          text = "Your message not containing text!"
        )
        .exec
        .void
  }

  private def commandControl(message: String, chatId: ChatIntId): F[String] =
    message.split(" +") match {
      case Array(head: String, tail: String) if head equals "/add" =>
        commandController.addAuthorCommand(Author(tail), chatId)
      case Array(head: String, tail: String) if head equals "/delete" =>
        commandController.deleteAuthorCommand(Author(tail), chatId)
      case Array(head: String) if head equals "/list" =>
        commandController.getListAuthorsCommand(chatId)
      case Array(head: String) if head equals "/start" => commandController.startCommand
      case _                                           => commandController.helpCommand
    }

  def sendArticleNotificationsList(articleNotifications: List[ArticleNotification]): F[Unit] =
    articleNotifications
      .traverse(articleNotification => sendArticleNotification(articleNotification))
      .void

  private def sendArticleNotification(articleNotification: ArticleNotification): F[Unit] =
    Methods
      .sendMessage(
        chatId = ChatIntId(articleNotification.chatId),
        text = articleNotification.message
      )
      .exec
      .void

}
