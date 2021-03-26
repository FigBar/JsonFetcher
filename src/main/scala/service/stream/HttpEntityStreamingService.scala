package service.stream

import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.{ Flow, Source }
import http.RestApiSource
import io.circe.Error
import serialization.JsonSerializable
import scala.concurrent.Future

case class HttpEntityStreamingService[A]()(
    implicit serializer: JsonSerializable[A]
) extends EntityStreamingService[A] {

  override def entityStream(
      res: Future[HttpResponse]
  ): Source[Either[Error, A], Future[Any]] =
    RestApiSource
      .byteStringStream(res)
      .collect { case Right(bs) => bs }
      .map(serializer.fromJsonByteString)
      .via(logDecodedResourceFlow)

  private def logDecodedResourceFlow =
    Flow[Either[Error, A]].map { resource =>
      resource match {
        case Left(error) => scribe.error(error)
        case Right(json) =>
          scribe.debug(s"Received following json resource: $json")
      }
      resource
    }

}
