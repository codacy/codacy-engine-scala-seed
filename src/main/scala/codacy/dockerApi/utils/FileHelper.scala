package codacy.dockerApi.utils

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

object FileHelper {

  def createTmpFile(content: String, prefix: String = "config", suffix: String = ".conf"): Path = {
    Files.write(
      Files.createTempFile(prefix, suffix),
      content.getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE
    )
  }

  def stripPath(filename: Path, prefix: Path): String = {
    stripPath(filename.toString, prefix.toString)
  }

  def stripPath(filename: String, prefix: String): String = {
    filename.stripPrefix(prefix)
      .stripPrefix("/")
  }

  def listAllFiles(path: String): List[File] = {
    listAllFiles(Paths.get(path))
  }

  def listAllFiles(path: Path): List[File] = {
    recursiveListFiles(path.toFile)
  }

  private def recursiveListFiles(file: File): List[File] = {
    val these = file.listFiles
    (these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)).toList
  }

}
