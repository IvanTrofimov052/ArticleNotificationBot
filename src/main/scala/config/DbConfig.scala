package config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.*

final case class DbConfig(
  url: String,
  driver: String,
  user: String,
  password: String
)

object DbConfig {
  implicit val reader: ConfigReader[DbConfig] = deriveReader
}
