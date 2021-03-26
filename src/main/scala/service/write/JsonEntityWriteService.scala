package service.write

import akka.stream.scaladsl.{ FileIO, Source }
import akka.stream.{ IOResult, Materializer }
import domain.Entity
import serialization.JsonSerializable
import java.nio.file.{ Files, Paths }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

case class JsonEntityWriteService[A <: Entity]()(
    implicit serializer: JsonSerializable[A],
    mat: Materializer,
    exc: ExecutionContext
) extends EntityWriteService[A] {

  override def writeEntities(
      writeDir: String
  ): Seq[A] => Future[FileWriteResult] = entities => {
    if (entities.nonEmpty) Files.createDirectories(Paths.get(writeDir))
    val writeResults = entities
      .map(toWriteRecord(_, writeDir))
      .map(writeFile)
    Future
      .foldLeft(writeResults)(List.empty[IOResult])((acc, r) => r :: acc)
      .map(summarizeWriteResults)
  }

  private def toWriteRecord(element: A, writeDir: String): WriteRecord =
    WriteRecord(
      serializer.toJsonByteString(element),
      s"$writeDir${element.id}${JsonEntityWriteService.fileExtension}"
    )

  def writeFile(file: WriteRecord): Future[IOResult] =
    Source
      .single(file.data)
      .runWith(FileIO.toPath(Paths.get(file.filePath)))

  def summarizeWriteResults(res: Seq[IOResult]): FileWriteResult = {
    val (success, failure) = res.foldLeft[(Int, Int)](0, 0)((acc, el) =>
      el.status match {
        case Success(_) => (acc._1 + 1, acc._2)
        case Failure(_) => (acc._1, acc._2 + 1)
      }
    )
    FileWriteResult(success, failure)
  }
}

object JsonEntityWriteService {
  private val fileExtension: String = ".json"
}
