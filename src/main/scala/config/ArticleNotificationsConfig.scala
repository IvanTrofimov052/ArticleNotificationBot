package config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class ArticleNotificationsConfig(url: String)

object ArticleNotificationsConfig {
  implicit val reader: ConfigReader[ArticleNotificationsConfig] = deriveReader
}
