package client

import domain.errors.*

trait ArticleNotificationClient[F[_]] {
  def getArticleNotifications: F[Either[AppError, List[ArticleNotification]]]
}
