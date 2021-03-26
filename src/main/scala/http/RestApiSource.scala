package http

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.stream.scaladsl.{ Flow, Source }
import akka.util.ByteString

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object RestApiSource {

  def byteStringStream(
      res: Future[HttpResponse]
  ): Source[Either[HttpResponse, ByteString], Future[Any]] =
    Source
      .futureSource(res.map {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          entity.dataBytes
            .via(EntityStreamingSupport.json().framingDecoder)
            .map(Right(_))
        case res =>
          Source.single(Left(res))
      })
      .via(logHttpError)

  private def logHttpError[A] =
    Flow[Either[HttpResponse, ByteString]].map { resource =>
      resource match {
        case Left(res) =>
          scribe.error(s"Http request failed: $res")
        case _ => ()
      }
      resource
    }

}
