package service.stream

import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.Source
import io.circe.Error
import scala.concurrent.Future

trait EntityStreamingService[A] {
  def entityStream(
      res: Future[HttpResponse]
  ): Source[Either[Error, A], Future[Any]]
}
