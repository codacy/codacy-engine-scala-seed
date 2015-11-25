package codacy.dockerApi.utils

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

    java.nio.file.Paths.get(fileTmp).getFileName.toString should startWith("prefix")
    fileTmp should endWith(".ext")
    io.Source.fromFile(fileTmp).mkString should be("foo")
  }
}
