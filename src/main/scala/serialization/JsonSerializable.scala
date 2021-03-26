package serialization

import akka.util.ByteString
import io.circe.Error

trait JsonSerializable[A] {
  def fromJsonByteString(resource: ByteString): Either[Error, A]
  def toJsonByteString(resource: A): ByteString
  def toJsonString(resource: A): String
}
