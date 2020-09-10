val scala211 = "2.11.12"
val scala212 = "2.12.10"
val scala213 = "2.13.1"

val specs2Version = "4.8.3"

ThisBuild / organization := "com.codacy"
ThisBuild / scalaVersion := scala212
ThisBuild / crossScalaVersions := Seq(scala211, scala212, scala213)

name := "codacy-engine-scala-seed"

libraryDependencies ++= Seq(("com.typesafe.play" %% "play-json" % "2.7.4").withSources(),
                            ("com.codacy" %% "codacy-plugins-api" % "5.0.0").withSources(),
                            ("com.github.pathikrit" %% "better-files" % "3.8.0").withSources(),
                            "org.specs2" %% "specs2-core" % specs2Version % Test,
                            "org.specs2" %% "specs2-mock" % specs2Version % Test)

scalacOptions := Seq()

// HACK: This setting is not picked up properly from the plugin
pgpPassphrase := Option(System.getenv("SONATYPE_GPG_PASSPHRASE")).map(_.toCharArray)

description := "Library to develop Codacy tools"

scmInfo := Some(
  ScmInfo(url("https://github.com/codacy/codacy-engine-scala-seed"),
          "scm:git:git@github.com:codacy/codacy-engine-scala-seed.git")
)

publicMvnPublish
