import sbt._

resolvers += Resolver.sonatypeRepo("releases")

name := """codacy-engine-scala-seed"""

organization := "com.codacy"

version := "2.7.1"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.10.5", scalaVersion.value)

scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-Ywarn-adapted-args", "-Xlint")

resolvers += "Bintray Typesafe Repo" at "http://dl.bintray.com/typesafe/maven-releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.4.8",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.codacy" %% "codacy-plugins-api" % "1.0.8" withSources(),
  "com.github.pathikrit" %% "better-files" % "2.16.0" withSources()
)

organizationName := "Codacy"

organizationHomepage := Some(new URL("https://www.codacy.com"))

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

startYear := Some(2015)

description := "Library to develop Codacy tool plugins"

licenses := Seq("The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("http://www.github.com/codacy/codacy-engine-scala-seed/"))

pomExtra :=
  <scm>
    <url>http://www.github.com/codacy/codacy-engine-scala-seed</url>
    <connection>scm:git:git@github.com:codacy/codacy-engine-scala-seed.git</connection>
    <developerConnection>scm:git:https://github.com/codacy/codacy-engine-scala-seed.git</developerConnection>
  </scm>
    <developers>
      <developer>
        <id>johannegger</id>
        <name>Johann</name>
        <email>johann [at] codacy.com</email>
        <url>https://github.com/johannegger</url>
      </developer>
    </developers>
