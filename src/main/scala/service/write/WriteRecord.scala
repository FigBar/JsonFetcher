package service.write

import akka.util.ByteString

case class WriteRecord(
    data: ByteString,
    filePath: String
)
