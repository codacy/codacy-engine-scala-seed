import sbt._
import sbt.Keys._

val scalaBinaryVersionNumber = "2.12"
val scalaVersionNumber = s"$scalaBinaryVersionNumber.4"

lazy val codacyEngineScalaSeed = project
  .in(file("."))
  .settings(
    inThisBuild(
      List(organization := "com.codacy",
           scalaVersion := scalaVersionNumber,
           version := "1.0.0-SNAPSHOT",
           scalacOptions ++= Common.compilerFlags,
           scalacOptions in Test ++= Seq("-Yrangepos"),
           scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings"))
    ),
    name := "codacy-engine-scala-seed",
    // App Dependencies
    libraryDependencies ++= Seq(Dependencies.playJson, Dependencies.codacyPluginsApi, Dependencies.betterFiles),
    // Test Dependencies
    libraryDependencies ++= Dependencies.specs2
  )
  .settings(Common.genericSettings: _*)

// Scapegoat
scalaVersion in ThisBuild := scalaVersionNumber
scalaBinaryVersion in ThisBuild := scalaBinaryVersionNumber
scapegoatDisabledInspections in ThisBuild := Seq()
scapegoatVersion in ThisBuild := "1.3.5"

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
