package flow

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import config.JsonFetcherConfig
import domain.Entity
import service.stream.EntityStreamingService
import service.write.{ EntityWriteService, FileWriteResult }

import scala.concurrent.{ ExecutionContext, Future }

case class JsonEntityFetchFlow[A <: Entity](
    config: JsonFetcherConfig,
    httpClient: HttpExt,
    httpStreamingService: EntityStreamingService[A],
    writeService: EntityWriteService[A]
)(implicit exc: ExecutionContext, mat: Materializer) {

  def fetchAndWrite: Future[FileWriteResult] =
    httpStreamingService
      .entityStream(httpClient.singleRequest(HttpRequest(uri = config.url)))
      .collect { case Right(json) => json }
      .runWith(Sink.seq)
      .flatMap(writeService.writeEntities(config.writeDir))
}
