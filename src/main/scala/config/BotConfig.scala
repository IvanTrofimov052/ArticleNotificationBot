package config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class BotConfig(baseUrl: String)

object BotConfig {
  implicit val BotConfig: ConfigReader[BotConfig] = deriveReader
}
