name := "JsonFetcher"

version := "0.1"

scalaVersion := "2.13.5"

val CirceVersion = "0.12.3"
val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.4"
val PureConfigVersion = "0.14.1"
val ScribeVersion = "3.4.0"
val ScalaTestVersion = "3.2.5"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % CirceVersion)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
  "com.outr" %% "scribe" % ScribeVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test
)
