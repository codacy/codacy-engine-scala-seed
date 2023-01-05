package com.codacy.tools.scala.seed.utils

import java.nio.charset.StandardCharsets
import java.nio.file.{Path, StandardOpenOption}
import java.nio.file.Files
import scala.collection.JavaConverters._

object FileHelper {

  /**
    * Create temporary file with a certain content
    */
  def createTmpFile(content: String, prefix: String = "config", suffix: String = ".conf"): Path = {
    val tmpFile = Files.createTempFile(prefix, suffix)
    Files.write(tmpFile, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE)
    tmpFile
  }

  /**
    * Strip prefix from an absolute path guaranteeing it does not start with a slash (/)
    */
  def stripAbsolutePrefix(path: String, prefix: String): String = {
    path
      .stripPrefix(prefix)
      .stripPrefix("/")
  }

  /**
    * Find the configuration file path
    *
    * @param root path to search recursively
    * @param configFileNames to match the files while searching
    * @param maxDepth to search
    * @return config file path closer to the root
    */
  def findConfigurationFile(root: Path, configFileNames: Set[String], maxDepth: Int = 5): Option[Path] = {
    Files
      .walk(root, maxDepth)
      .iterator()
      .asScala
      .find { file =>
        val fileName = Option(file.getFileName) match {
          case Some(path) => path.toString
          case None => ""
        }
        configFileNames.contains(fileName)
      }
  }

}
