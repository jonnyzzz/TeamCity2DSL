package org.jonnyzzz.teamcity.dsl.gradle

import org.junit.Assert
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object Paths {
  private val testDataPath by lazy {
    val testDataPath = System.getProperty("TEST_DATA_HOME")
    Assert.assertNotNull("Test data dir property should be set", testDataPath)

    println("TestDataPath: $testDataPath")

    val testData = File(testDataPath)
    Assert.assertTrue("Test data dir $testDataPath should exist", testData.exists())
    testData
  }

  fun teamcityProjectTestDataPath(name : String) : File {
    val path = (testDataPath / ".." / "tests-integration" / name / "teamcity").canonicalFile
    println("Path: $path")
    Assert.assertTrue("Test data dir $path should exist", path.isDirectory)
    return path
  }

  fun path(name : String) : File {
    val result = testDataPath / name
    Assert.assertTrue("File $this should exist", result.exists())
    return result
  }

  fun copyRec(testCase : File, toHome : File) {
    val homePath = toHome.toPath()

    println("TestCase: ${testCase}")
    val testDataPath = testCase.toPath()
    Files.walk(testDataPath).forEach {
      println("TestCase Files: " + it)

      Files.copy(it, homePath.resolve(testDataPath.relativize(it)), StandardCopyOption.REPLACE_EXISTING)
    }
  }
}