package service.write

import domain.Entity
import scala.concurrent.Future

trait EntityWriteService[A <: Entity] {

  def writeEntities(writeDir: String): Seq[A] => Future[FileWriteResult]

}
