package config

import cats.effect.Sync
import pureconfig.*
import pureconfig.generic.semiauto.*

final case class AppConfig(
  db: DbConfig,
  articleNotifications: ArticleNotificationsConfig,
  bot: BotConfig
)

object AppConfig {

  implicit val reader: ConfigReader[AppConfig] = deriveReader

  def load[F[_]: Sync]: F[AppConfig] =
    Sync[F].delay(ConfigSource.default.loadOrThrow[AppConfig])

}
