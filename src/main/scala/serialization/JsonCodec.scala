package serialization

import akka.util.ByteString
import io.circe.{ Decoder, Encoder }
import io.circe.parser.decode
import io.circe.Printer
import io.circe.syntax._
import io.circe.Error

trait JsonCodec[T] {
  implicit def encoder: Encoder[T]
  implicit def decoder: Decoder[T]

  implicit private val printer: Printer =
    Printer.spaces2.copy(dropNullValues = true)

  def fromJson(resource: ByteString): Either[Error, T] =
    decode[T](resource.utf8String)

  def toByteStringJson(obj: T): ByteString =
    ByteString(toJson(obj))

  def toJson(obj: T): String =
    printer.print(obj.asJson)

}
