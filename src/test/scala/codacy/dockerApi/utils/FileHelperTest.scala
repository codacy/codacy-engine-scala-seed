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
}