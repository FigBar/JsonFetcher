package service.write

import akka.actor.ActorSystem
import domain.Post
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatest.flatspec.AnyFlatSpec
import java.io.File
import java.nio.charset.StandardCharsets
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import java.nio.file.{ Files, Path }
import scala.concurrent.ExecutionContext
import scala.reflect.io.Directory

class JsonEntityWriteServiceSpec
    extends AnyFlatSpec
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with ScalaFutures
    with Matchers {
  implicit val system: ActorSystem = ActorSystem("test_system")
  implicit val ex: ExecutionContext = system.dispatcher
  val testPath: String = JsonEntityWriteServiceSpec.testPath

  val post1: Post = Post(1, 1, "post_title", "body")
  val post2: Post = Post(2, 2, "post_title", "body")
  val testPosts: List[Post] = post1 :: post2 :: Nil
  val writeService: JsonEntityWriteService[Post] = JsonEntityWriteService[Post]

  override protected def afterAll(): Unit =
    system.terminate()

  override protected def afterEach(): Unit = {
    deleteDirectory(testPath)
  }

  "JsonEntityWriteService" should "write Post entities to separate files with correct names" in {
    whenReady(writeService.writeEntities(testPath)(testPosts)) { res =>
      res.successCount shouldBe 2
      res.failureCount shouldBe 0
      getFileCount(testPath) shouldBe 2
      getFileNames(testPath) shouldEqual testPosts.map(p => s"${p.id}.json")
    }
  }

  "JsonEntityWriteService" should "write Post entities to separate files with correct contents" in {
    whenReady(writeService.writeEntities(testPath)(testPosts)) { _ =>
      getFileContents(testPath) shouldEqual testPosts.map(Post.toJson)
    }
  }

  "JsonEntityWriteService" should "not create directory for data when the sequence of entities is empty" in {
    whenReady(writeService.writeEntities(testPath)(Nil)) { _ =>
      Files.exists(Path.of(testPath)) shouldEqual false
    }
  }

  private def getDirectory(path: String) =
    new File(path)

  private def deleteDirectory(path: String): Boolean =
    Directory(getDirectory(path)).deleteRecursively()

  private def getFileCount(path: String): Int =
    getDirectory(path).list().length

  private def getFiles(path: String): Array[File] =
    getDirectory(path).listFiles()

  private def getFileNames(path: String): Array[String] =
    getFiles(path).map(_.getName)

  private def getFileContents(path: String): Array[String] =
    getFileNames(path).map(f => s"$testPath/$f").map(getFileContent)

  private def getFileContent(filePath: String) =
    Files.readString(Path.of(filePath), StandardCharsets.UTF_8)

}

object JsonEntityWriteServiceSpec {
  val testPath = "./src/test/data/"
}
