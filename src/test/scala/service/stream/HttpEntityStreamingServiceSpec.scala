package service.stream

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ HttpEntity, HttpResponse, StatusCodes }
import akka.stream.scaladsl.Sink
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.TestProbe
import akka.util.ByteString
import domain.Post
import io.circe.ParsingFailure
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import serialization.JsonSerializable
import scala.concurrent.{ ExecutionContext, Future }

class HttpEntityStreamingServiceSpec
    extends AnyFlatSpec
    with BeforeAndAfterAll {
  implicit val system: ActorSystem = ActorSystem("test_system")
  implicit val ex: ExecutionContext = system.dispatcher

  val post1: Post = Post(1, 1, "post_title", "body")
  val post2: Post = Post(2, 2, "post_title", "body")
  val testPosts: List[Post] = post1 :: post2 :: Nil

  val streamService: HttpEntityStreamingService[Post] =
    HttpEntityStreamingService[Post]

  override protected def afterAll(): Unit =
    system.terminate()

  "HttpEntityStreamingService" should "correctly decode valid json Post sequence" in {
    streamService
      .entityStream(correctResponse(testPosts))
      .runWith(TestSink.probe)
      .request(2)
      .expectNext(Right(post1), Right(post2))
  }

  "HttpEntityStreamingService" should "parse all valid Posts present in response" in {
    val probe = TestProbe()
    streamService
      .entityStream(correctResponse(testPosts ::: testPosts ::: testPosts))
      .to(Sink.actorRef(probe.ref, "completed", _ => "failed"))
      .run()
    probe.receiveN(6)
  }

  "HttpEntityStreamingService" should "return parsing errors given invalid json Post sequence" in {
    val probe = TestProbe()
    streamService
      .entityStream(invalidResponse[Post](post1, "///"))
      .collect { case Left(err) => err }
      .to(Sink.actorRef(probe.ref, "completed", _ => "failed"))
      .run()
    probe.expectMsgType[ParsingFailure]
  }

  "HttpEntityStreamingService" should "complete successfully with no values emitted after receiving http error" in {
    val probe = TestProbe()
    streamService
      .entityStream(correctResponse[Post](Nil))
      .to(Sink.actorRef(probe.ref, "completed", _ => "failed"))
      .run()
    probe.receiveN(0)
    probe.expectMsg("completed")
  }

  "HttpEntityStreamingService" should "complete successfully when there are no entities in response" in {
    val probe = TestProbe()
    streamService
      .entityStream(Future.successful(HttpResponse(StatusCodes.BadRequest)))
      .to(Sink.actorRef(probe.ref, "completed", _ => "failed"))
      .run()
    probe.receiveN(0)
    probe.expectMsg("completed")
  }

  private def correctResponse[A](
      seq: Seq[A]
  )(implicit ser: JsonSerializable[A]): Future[HttpResponse] = {
    val concat = seq
      .map(ser.toJsonByteString)
      .foldLeft(ByteString.empty)((acc, x) => acc.concat(x))
    Future.successful(HttpResponse(entity = HttpEntity(data = concat)))
  }

  private def invalidResponse[A](
      entity: A,
      replacementToken: String
  )(
      implicit ser: JsonSerializable[A]
  ): Future[HttpResponse] = {
    val malformedEntity = ByteString(
      ser.toJsonString(entity).replaceFirst(":", replacementToken)
    )
    Future.successful(HttpResponse(entity = HttpEntity(data = malformedEntity)))
  }
}
