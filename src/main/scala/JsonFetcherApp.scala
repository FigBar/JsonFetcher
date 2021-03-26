import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import config.JsonFetcherConfig
import pureconfig.generic.auto._
import domain.{ Entity, Post }
import flow.JsonEntityFetchFlow
import pureconfig.ConfigSource
import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }
import serialization.JsonSerializable
import service.stream.HttpEntityStreamingService
import service.write.JsonEntityWriteService

object JsonFetcherApp extends App {

  implicit val system: ActorSystem = ActorSystem("JsonFetcherApp")
  implicit val exc: ExecutionContext = system.dispatcher
  private val httpClient = Http()

  ConfigSource.default.load[JsonFetcherConfig] match {
    case Left(error) =>
      println(error)
      system.terminate()
    case Right(config) => run[Post](config)
  }

  private def run[A <: Entity](
      config: JsonFetcherConfig
  )(implicit serializer: JsonSerializable[A]): Unit = {
    val httpStreamService = HttpEntityStreamingService[A]
    val writeService = JsonEntityWriteService[A]
    JsonEntityFetchFlow(
      config,
      httpClient,
      httpStreamService,
      writeService
    ).fetchAndWrite
      .onComplete {
        case Failure(exc) =>
          scribe.error(exc.getMessage)
          onShutdown()
        case Success(writeResult) =>
          scribe.info(
            s"Successfully written ${writeResult.successCount} json resources"
          )
          scribe.info(s"${writeResult.failureCount} write errors occurred")
          onShutdown()
      }
  }

  private def onShutdown(): Unit = {
    httpClient.shutdownAllConnectionPools()
    system.terminate()
  }
}
