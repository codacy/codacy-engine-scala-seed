val scala212 = "2.12.14"
val scala213 = "2.13.6"

ThisBuild / organization := "com.codacy"
ThisBuild / scalaVersion := scala212
ThisBuild / crossScalaVersions := Seq(scala212, scala213)

name := "codacy-engine-scala-seed"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.9.2",
  "com.codacy" %% "codacy-plugins-api" % "8.1.1",
  "org.scalameta" %% "munit" % "1.0.0-M7" % Test
)

scalacOptions := Seq()

// HACK: This setting is not picked up properly from the plugin
pgpPassphrase := Option(System.getenv("SONATYPE_GPG_PASSPHRASE")).map(_.toCharArray)

description := "Library to develop Codacy tools"

scmInfo := Some(
  ScmInfo(url("https://github.com/codacy/codacy-engine-scala-seed"),
          "scm:git:git@github.com:codacy/codacy-engine-scala-seed.git")
)

publicMvnPublish
