package domain

import akka.util.ByteString
import io.circe.{ Decoder, Encoder, Error }
import serialization.{ JsonCodec, JsonSerializable }
import io.circe.generic.semiauto._

case class Post(
    id: Int,
    userId: Int,
    title: String,
    body: String
) extends Entity

object Post extends JsonCodec[Post] {
  override implicit def encoder: Encoder[Post] = deriveEncoder
  override implicit def decoder: Decoder[Post] = deriveDecoder

  implicit val postSerializable: JsonSerializable[Post] =
    new JsonSerializable[Post] {
      override def fromJsonByteString(
          resource: ByteString
      ): Either[Error, Post] =
        fromJson(resource)

      override def toJsonByteString(resource: Post): ByteString =
        toByteStringJson(resource)

      override def toJsonString(resource: Post): String =
        toJson(resource)
    }
}
