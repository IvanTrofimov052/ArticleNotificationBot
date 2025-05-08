package client

import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}

final case class ArticleNotification(chatId: Int, message: String)

object ArticleNotification {

  implicit val encoder: Encoder[ArticleNotification] = deriveEncoder[ArticleNotification]
  implicit val decoder: Decoder[ArticleNotification] = deriveDecoder[ArticleNotification]

}
