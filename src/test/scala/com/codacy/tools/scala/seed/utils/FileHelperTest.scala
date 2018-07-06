package com.codacy.tools.scala.seed.utils

import java.nio.file.Path

import better.files.File
import org.specs2.mutable.Specification

class FileHelperTest extends Specification {

  "FileHelper" >> {

    "createTmpFile" >> {
      "create a temporary file and write the content to it" >> {
        val file = FileHelper.createTmpFile("content", "prefix", ".ext")

        File(file).name must startWith("prefix").and(endWith(".ext"))
        File(file).contentAsString must beEqualTo("content")
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
          (for {
            root <- File.temporaryDirectory()
          } yield {
            root./("test.json").write("content")

            val configFile: Option[Path] =
              FileHelper.findConfigurationFile(root.path, configFileNames = Set("test.json"))

            configFile must beEqualTo(Option(root./("test.json").path))
          }).get()
        }

        "not find the configuration file closest to the root if its deeper then 5 in the path" >> {
          (for {
            root <- File.temporaryDirectory()
          } yield {
            val subDirectory: File = root / "one" / "two" / "three" / "four" / "five"
            subDirectory.createDirectories()
            subDirectory./("test.json").write("content")

            val configFile: Option[Path] =
              FileHelper.findConfigurationFile(root.path, configFileNames = Set("test.json"))

            configFile must beEqualTo(Option.empty[Path])
          }).get()
        }
      }

    }

  }

}
