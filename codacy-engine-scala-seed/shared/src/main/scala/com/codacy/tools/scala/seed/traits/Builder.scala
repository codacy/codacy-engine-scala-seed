package com.codacy.tools.scala.seed.traits

import java.io.File
import java.nio.file.Path

import com.codacy.tools.scala.seed.utils.CommandRunner

import scala.util.{Failure, Success, Try}

sealed trait Builder {

  val command: List[String]
  val pathComponents: Seq[String]

  def supported(path: Path): Boolean

  def targetOfDirectory(path: File): Option[String]

  private def buildWithCommand(command: List[String], path: Path): Try[Boolean] = {
    CommandRunner.exec(command, dir = Option(path.toFile)) match {
      case Left(failure) => Failure(failure)
      case Right(output) if output.exitCode != 0 =>
        Failure(new Exception("Can't compile project."))

      case Right(_) => Success(true)
    }
  }

  def build(path: Path): Try[Boolean] = {
    buildWithCommand(command, path)
  }
}

object MavenBuilder extends Builder {
  val command = List("mvn", "compile")
  val pathComponents = Seq("src", "main", "java")

  def supported(path: Path): Boolean = {
    path.toFile.list.contains("pom.xml")
  }

  def targetOfDirectory(path: File): Option[String] = {
    Some(Seq(path.getAbsolutePath, "target", "classes").mkString(File.separator))
  }

}

object SBTBuilder extends Builder {

  val command = List("sbt", "compile")
  val pathComponents = Seq("src", "main", "scala")

  def supported(path: Path): Boolean = {
    path.toFile.list.contains("build.sbt")
  }

  def targetOfDirectory(path: File): Option[String] = {
    val target = new File(path.getAbsolutePath, "target")
    Option(target.exists).flatMap {
      case true =>
        val potentialScalaDir = target.list.find(filepath => filepath.startsWith("scala-"))
        potentialScalaDir.map(scalaDirectory => target.toPath.resolve(scalaDirectory).resolve("classes").toString)
      case false =>
        Option.empty
    }
  }

}
