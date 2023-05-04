package com.codacy.tools.scala.seed.utils

import java.nio.file.Path

import java.nio.file.Files

class FileHelperTest extends munit.FunSuite {
  test("createTmpFile: create a temporary file and write the content to it") {
    val file = FileHelper.createTmpFile("content", "prefix", ".ext")

    val fileName = file.getFileName.toString
    assert(fileName.startsWith("prefix"))
    assert(fileName.endsWith(".ext"))
    assertEquals(new String(Files.readAllBytes(file)), "content")
  }

  test("stripAbsolutePrefix: strip prefix from an absolute path guaranteeing it does not start with a slash") {
    val pathString: String = "a/b/c/filename.ext"
    val pathStringPrefix: String = "a/b"
    val result: String = FileHelper.stripAbsolutePrefix(pathString, pathStringPrefix)
    assertEquals(result, "c/filename.ext")

    val pathString2: String = "/a/b/filename.ext"
    val pathStringPrefix2: String = "/a/b/"
    val result2: String = FileHelper.stripAbsolutePrefix(pathString2, pathStringPrefix2)
    assertEquals(result2, "filename.ext")
  }

  test("stripAbsolutePrefix: stripPath with no common prefix") {
    val pathString: String = "filename.ext"
    val result: String = FileHelper.stripAbsolutePrefix(pathString, "/a/b/")
    assertEquals(result, pathString)

    val pathString2: String = "c/d/filename.ext"
    val result2: String = FileHelper.stripAbsolutePrefix(pathString2, "/a/b/")
    assertEquals(result2, pathString2)
  }

  test("findConfigurationFile: find the configuration file closest to the root") {
    val root = Files.createTempDirectory("")
    try {
      Files.write(root.resolve("test.json"), "content".getBytes("UTF-8"))

      val configFile: Option[Path] =
        FileHelper.findConfigurationFile(root, configFileNames = Set("test.json"))

      assertEquals(configFile, Some(root.resolve("test.json")))
    } finally {
      deleteRecursively(root)
    }
  }

  test(
    "findConfigurationFile: find the configuration file closest to the root with two possible configuration file names"
  ) {
    val root = Files.createTempDirectory("")
    try {
      Files.write(root.resolve("test2.json"), "content".getBytes("UTF-8"))

      val configFile: Option[Path] =
        FileHelper.findConfigurationFile(root, configFileNames = Set("test.json", "test2.json"))

      assertEquals(configFile, Some(root.resolve("test2.json")))
    } finally {
      deleteRecursively(root)
    }
  }

  test("findConfigurationFile: not find the configuration file closest to the root if its deeper then 5 in the path") {
    val root = Files.createTempDirectory("")
    try {
      val subDirectory = root.resolve("one").resolve("two").resolve("three").resolve("four").resolve("five")
      Files.createDirectories(subDirectory)
      Files.write(subDirectory.resolve("test.json"), "content".getBytes("UTF-8"))

      val configFile: Option[Path] =
        FileHelper.findConfigurationFile(root, configFileNames = Set("test.json"))

      assertEquals(configFile, None)
    } finally {
      deleteRecursively(root)
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
