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

  /**
    * Find the configuration file path
    *
    * @param root            path to search recursively
    * @param configFileNames to match the files while searching
    * @param maxDepth        to search
    * @return config file path closest to the root
    */
  def findConfigurationFile(root: Path, configFileNames: Set[String], maxDepth: Int = 5): Option[Path] = {
    val allFiles = better.files.File(root).walk(maxDepth = maxDepth)

    val configFiles: List[Path] = configFileNames.flatMap { nativeConfigFileName =>
      allFiles.filter(_.name == nativeConfigFileName).map(_.path)
    }(collection.breakOut)

    configFiles.sortBy(_.toString.length).headOption
  }

}
