import bot.ArticleTelegramBot
import cats.effect.*
import cats.effect.kernel.implicits.genSpawnOps
import cats.syntax.all.{toFlatMapOps, toFunctorOps}
import cats.{Monad, Parallel}
import client.{ArticleNotificationClient, HabrArticleNotifications}
import config.{AppConfig, BotConfig}
import controller.CommandController
import dao.ArticleSql
import doobie.util.transactor.Transactor
import org.http4s.blaze.client.BlazeClientBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sevice.ArticleStorage
import sttp.client4.httpclient.cats.HttpClientCatsBackend
import telegramium.bots.high.{Api, BotApi}

import scala.concurrent.duration.*

object Main extends IOApp {

  implicit def mainLogger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      logger <- Resource.eval(Slf4jLogger.create[IO])
      config <- Resource.eval(AppConfig.load[IO])
      sql = ArticleSql.make
      transactor = Transactor.fromDriverManager[IO](
        config.db.driver,
        config.db.url,
        config.db.user,
        config.db.password
      )
      storage: ArticleStorage[IO] = ArticleStorage.make(sql, transactor)
      client <- HttpClientCatsBackend.resource[IO]()
      getMessagesClient <- Resource.pure(
        HabrArticleNotifications.apply(config.articleNotifications, client)
      )
    } yield (getMessagesClient, storage, logger, config)).use((client, storage, logger, config) =>
      for {
        _ <- logger.info("main started")
        _ <- loop(CommandController.make(storage), client, config.bot)
      } yield ExitCode.Success
    )

  private def loop[F[_]: Async: Parallel](
    commandController: CommandController[F],
    client: ArticleNotificationClient[F],
    config: BotConfig
  ): F[Unit] = BlazeClientBuilder[F].resource.use { httpClient =>
    implicit val api: Api[F] = BotApi(httpClient, baseUrl = config.baseUrl)
    val bot                  = new ArticleTelegramBot[F](commandController)

    for {
      fiber1 <- Monad[F]
        .iterateForeverM(0: Int) { _ =>
          client.getArticleNotifications
            .flatMap {
              case Left(error)  => Monad[F].unit
              case Right(value) => bot.sendArticleNotificationsList(value)
            }
            .flatMap(_ => Temporal[F].sleep(1.minutes))
            .map(_ => 0)
        }
        .start
      fiber2 <- bot.start().start

      _ <- fiber1.join
      _ <- fiber2.join
    } yield ()
  }

}
