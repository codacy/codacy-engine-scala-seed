val scala212 = "2.12.17"
val scala213 = "2.13.10"
val scala3 = "3.3.0-RC5"

ThisBuild / organization := "com.codacy"
ThisBuild / scalaVersion := scala212
ThisBuild / crossScalaVersions := Seq(scala212, scala213, scala3)

lazy val `codacy-engine-scala-seed` =
  crossProject(JVMPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .settings(name := "codacy-engine-scala-seed",
              libraryDependencies ++= Seq("com.typesafe.play" %%% "play-json" % "2.10.0-RC8",
                                          "com.codacy" %%% "codacy-plugins-api" % "7.2.0",
                                          "org.scalameta" %%% "munit" % "1.0.0-M7" % Test),
              scalacOptions := Seq(),
              // HACK: This setting is not picked up properly from the plugin
              pgpPassphrase := Option(System.getenv("SONATYPE_GPG_PASSPHRASE")).map(_.toCharArray),
              description := "Library to develop Codacy tools",
              scmInfo := Some(
                ScmInfo(url("https://github.com/codacy/codacy-engine-scala-seed"),
                        "scm:git:git@github.com:codacy/codacy-engine-scala-seed.git")
              ),
              publicMvnPublish)

lazy val root =
  project
    .in(file("."))
    .settings(publish / skip := true)
    .aggregate(`codacy-engine-scala-seed`.jvm, `codacy-engine-scala-seed`.native)
