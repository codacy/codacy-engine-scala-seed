package com.codacy.tools.scala.seed.utils

import java.nio.file.Path

import org.specs2.mutable.Specification
import java.nio.file.Files

class FileHelperTest extends Specification {

  "FileHelper" >> {

    "createTmpFile" >> {
      "create a temporary file and write the content to it" >> {
        val file = FileHelper.createTmpFile("content", "prefix", ".ext")

        file.getFileName.toString must startWith("prefix").and(endWith(".ext"))
        new String(Files.readAllBytes(file)) must beEqualTo("content")
      }

      "stripAbsolutePrefix" >> {
        "strip prefix from an absolute path guaranteeing it does not start with a slash" >> {
          val pathString: String = "a/b/c/filename.ext"
          val pathStringPrefix: String = "a/b"
          val result: String = FileHelper.stripAbsolutePrefix(pathString, pathStringPrefix)
          result must beEqualTo("c/filename.ext")

          val pathString2: String = "/a/b/filename.ext"
          val pathStringPrefix2: String = "/a/b/"
          val result2: String = FileHelper.stripAbsolutePrefix(pathString2, pathStringPrefix2)
          result2 must beEqualTo("filename.ext")
        }

        "stripPath with no common prefix" >> {
          val pathString: String = "filename.ext"
          val result: String = FileHelper.stripAbsolutePrefix(pathString, "/a/b/")
          result must beEqualTo(pathString)

          val pathString2: String = "c/d/filename.ext"
          val result2: String = FileHelper.stripAbsolutePrefix(pathString2, "/a/b/")
          result2 must beEqualTo(pathString2)
        }
      }

      "findConfigurationFile" >> {
        "find the configuration file closest to the root" >> {
          val root = Files.createTempDirectory("")
          try {
            Files.write(root.resolve("test.json"), "content".getBytes("UTF-8"))

            val configFile: Option[Path] =
              FileHelper.findConfigurationFile(root, configFileNames = Set("test.json"))

            configFile must beEqualTo(Option(root.resolve("test.json")))
          } finally {
            deleteRecursively(root)
          }
        }

        "find the configuration file closest to the root with two possible configuration file names" >> {
          val root = Files.createTempDirectory("")
          try {
            Files.write(root.resolve("test2.json"), "content".getBytes("UTF-8"))

            val configFile: Option[Path] =
              FileHelper.findConfigurationFile(root, configFileNames = Set("test.json", "test2.json"))

            configFile must beEqualTo(Option(root.resolve("test2.json")))
          } finally {
            deleteRecursively(root)
          }
        }

        "not find the configuration file closest to the root if its deeper then 5 in the path" >> {
          val root = Files.createTempDirectory("")
          try {
            val subDirectory = root.resolve("one").resolve("two").resolve("three").resolve("four").resolve("five")
            Files.createDirectories(subDirectory)
            Files.write(subDirectory.resolve("test.json"), "content".getBytes("UTF-8"))

            val configFile: Option[Path] =
              FileHelper.findConfigurationFile(root, configFileNames = Set("test.json"))

            configFile must beEqualTo(Option.empty[Path])
          } finally {
            deleteRecursively(root)
          }
        }
      }

    }

  }

  def deleteRecursively(directory: Path): Unit = {
    Files.list(directory).forEach { file =>
      if (Files.isDirectory(file)) deleteRecursively(file)
      else Files.delete(file)
    }
    Files.delete(directory)
  }

}
