import sbt._
import sbt.Keys._

val scala211 = "2.11.12"
val scala212 = "2.12.10"
val scala213 = "2.13.1"

val specs2Version = "4.7.1"

lazy val codacyEngineScalaSeed = project
  .in(file("."))
  .settings(
    inThisBuild(
      Seq(
        organization := "com.codacy",
        scalaVersion := scala212,
        crossScalaVersions := Seq(scala211, scala212, scala213)
      )
    ),
    name := "codacy-engine-scala-seed",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.7.4" withSources (),
      "com.codacy" %% "codacy-plugins-api" % "3.0.80" withSources (),
      "com.github.pathikrit" %% "better-files" % "3.8.0" withSources (),
      "org.specs2" %% "specs2-core" % specs2Version % Test,
      "org.specs2" %% "specs2-mock" % specs2Version % Test
    )
  )

scapegoatVersion in ThisBuild := "1.3.10"

// Sonatype repository settings
credentials += Credentials("Sonatype Nexus Repository Manager",
                           "oss.sonatype.org",
                           sys.env.getOrElse("SONATYPE_USER", "username"),
                           sys.env.getOrElse("SONATYPE_PASSWORD", "password"))
publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ =>
  false
}
publishTo := sonatypePublishTo.value

organizationName := "Codacy"
organizationHomepage := Some(new URL("https://www.codacy.com"))
startYear := Some(2018)
description := "Library to develop Codacy metrics plugins"
licenses := Seq("AGPL-3.0" -> url("https://opensource.org/licenses/AGPL-3.0"))
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
      <developer>
        <id>rtfpessoa</id>
        <name>Rodrigo Fernandes</name>
        <email>rodrigo [at] codacy.com</email>
        <url>https://github.com/rtfpessoa</url>
      </developer>
      <developer>
        <id>bmbferreira</id>
        <name>Bruno Ferreira</name>
        <email>bruno.ferreira [at] codacy.com</email>
        <url>https://github.com/bmbferreira</url>
      </developer>
      <developer>
        <id>xplosunn</id>
        <name>Hugo Sousa</name>
        <email>hugo [at] codacy.com</email>
        <url>https://github.com/xplosunn</url>
      </developer>
      <developer>
        <id>pedrocodacy</id>
        <name>Pedro Amaral</name>
        <email>pamaral [at] codacy.com</email>
        <url>https://github.com/pedrocodacy</url>
      </developer>
    </developers>
