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

  fun path(name : String) : File {
    val result = testDataPath / name
    Assert.assertTrue("File $this should exist", result.exists())
    return result
  }

  fun copyTestData(name : String, toHome : File) {
    val homePath = toHome.toPath()


    val testCase = path(name)
    println("TestCase: ${testCase}")

    val testDataPath = testCase.toPath()
    Files.walk(testDataPath).forEach {
      println("TestCase Files: " + it)

      Files.copy(it, homePath.resolve(testDataPath.relativize(it)), StandardCopyOption.REPLACE_EXISTING)
    }
  }
}