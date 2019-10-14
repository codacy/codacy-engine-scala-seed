import sbt._

object Dependencies {

  val playJson = ("com.typesafe.play" %% "play-json" % "2.7.4").withSources()
  val codacyPluginsApi = ("com.codacy" %% "codacy-plugins-api" % "3.0.80").withSources()
  val betterFiles = ("com.github.pathikrit" %% "better-files" % "3.8.0").withSources()

  val specs2 = Seq("org.specs2" %% "specs2-core", "org.specs2" %% "specs2-mock").map(_ % "4.7.1" % Test)

}
