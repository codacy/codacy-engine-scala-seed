import sbt._

object Dependencies {

  val playJson = ("com.typesafe.play" %% "play-json" % "2.6.9").withSources()
  val codacyPluginsApi = ("com.codacy" %% "codacy-plugins-api" % "0.1.0-pre.versioned-api-SNAPSHOT").withSources()
  val betterFiles = ("com.github.pathikrit" %% "better-files" % "3.5.0").withSources()

  val specs2 = Seq("org.specs2" %% "specs2-core", "org.specs2" %% "specs2-mock").map(_ % "4.0.2" % Test)

}
