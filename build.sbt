name := "gatling-kafka"

organization := "com.github.mnogu"

version := "0.3.0-SNAPSHOT"

scalaVersion := "2.12.8"

resolvers += "Confluent" at "https://packages.confluent.io/maven/"

libraryDependencies ++= Seq(
  "io.gatling" % "gatling-core" % "3.1.2" % "provided",
  ("org.apache.kafka" % "kafka-clients" % "2.0.0")
  // Gatling contains slf4j-api
    .exclude("org.slf4j", "slf4j-api"),
  "com.sksamuel.avro4s" %% "avro4s-core" % "1.9.0",
  "io.confluent" % "kafka-avro-serializer" % "4.1.2"
)

// Gatling contains scala-library
assemblyOption in assembly := (assemblyOption in assembly).value
  .copy(includeScala = false)
