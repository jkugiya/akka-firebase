name := """akka-firebase"""

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.7",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.7" % "test",
  "com.google.firebase" % "firebase-server-sdk" % "3.0.1",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")
