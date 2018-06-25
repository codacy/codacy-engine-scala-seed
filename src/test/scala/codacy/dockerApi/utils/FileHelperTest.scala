package codacy.dockerApi.utils

import java.nio.file.Path

import better.files.File
import org.scalatest._

class FileHelperTest extends FlatSpec with Matchers {

  "FileHelper" should "stripPath from String" in {
    val pathString: String = "a/b/c/filename.ext"
    val pathStringPrefix: String = "a/b"

    val pathString2: String = "/a/b/filename.ext"
    val pathStringPrefix2: String = "/a/b/"

    val result: String = FileHelper.stripPath(pathString, pathStringPrefix)
    result should be("c/filename.ext")

    val result2: String = FileHelper.stripPath(pathString2, pathStringPrefix2)
    result2 should be("filename.ext")
  }

  "FileHelper" should "stripPath with no common prefix" in {
    val pathString: String = "filename.ext"
    val pathString2: String = "c/d/filename.ext"

    val result: String = FileHelper.stripPath(pathString, "/a/b/")
    result should be(pathString)

    val result2: String = FileHelper.stripPath(pathString2, "/a/b/")
    result2 should be(pathString2)
  }

  "FileHelper" should "stripPath from Path" in {
    val path: java.nio.file.Path = java.nio.file.Paths.get("a/b/c/filename.ext")
    val pathPrefix: java.nio.file.Path = java.nio.file.Paths.get("a/b")

    val path2: java.nio.file.Path = java.nio.file.Paths.get("/a/b/filename.ext")
    val pathPrefix2: java.nio.file.Path = java.nio.file.Paths.get("/a/b/")

    val result: String = FileHelper.stripPath(path, pathPrefix)
    result should be("c/filename.ext")

    val result2: String = FileHelper.stripPath(path2, pathPrefix2)
    result2 should be("filename.ext")
  }

  "FileHelper" should "createTmpFile" in {
    val fileTmp = FileHelper.createTmpFile("foo", "prefix", ".ext").toString

    java.nio.file.Paths.get(fileTmp).getFileName.toString should startWith(
      "prefix")
    fileTmp should endWith(".ext")
    io.Source.fromFile(fileTmp).mkString should be("foo")
  }

  "FileHelper#findConfigurationFile" should "find the configuration file closest to the root" in {
    (for {
      root <- File.temporaryDirectory()
    } yield {
      root./("test.json").write("content")

      val configFile: Option[Path] =
        FileHelper.findConfigurationFile(root.path,
                                         configFileNames = Set("test.json"))

      configFile should be(Option(root./("test.json").path))
    }).get()
  }

  "FileHelper#findConfigurationFile" should "not find the configuration file closest to the root if its deeper then 5 in the path" in {
    (for {
      root <- File.temporaryDirectory()
    } yield {
      val subDirectory: File = root / "one" / "two" / "three" / "four" / "five"
      subDirectory.createDirectories()
      subDirectory./("test.json").write("content")

      val configFile: Option[Path] =
        FileHelper.findConfigurationFile(root.path,
                                         configFileNames = Set("test.json"))

      configFile should be(Option.empty[Path])
    }).get()
  }

}
