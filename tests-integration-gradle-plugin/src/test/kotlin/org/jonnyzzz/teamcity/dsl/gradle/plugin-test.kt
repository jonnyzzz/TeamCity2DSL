package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

operator fun File.div(s:String) = File(this, s)

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
}


abstract class PluginTest(val testName : String) {
  lateinit var home : File

  @Rule
  @JvmField()
  val temp = TemporaryFolder()

  private fun runSuccessfulBuild(vararg args: String): BuildResult {
    home = temp.newFolder() / "gradle-test"
    home.mkdirs()
    val homePath = home.toPath()


    val testCase = Paths.path(testName)
    println("TestCase: ${testCase}")

    val testDataPath = testCase.toPath()
    Files.walk(testDataPath).forEach {
      println("TestCase Files: " + it)

      Files.copy(it, homePath.resolve(testDataPath.relativize(it)), StandardCopyOption.REPLACE_EXISTING)
    }

    Files.walk(homePath).forEach {
      println("Test data: " + it)
    }

    (home / "build.gradle").apply {
      val text = readText()
              .replace("\${DSL_PLUGIN_CLASSPATH}", System.getProperty("TEST_PLUGIN_CLASSPATH"))
              .replace("\${DSL_PLUGIN_NAME}", System.getProperty("TEST_PLUGIN_NAME"))
      println("Patched project script:\n$text\n")
      writeText(text)
    }

    return GradleRunner.create()
            .withDebug(true)
            .withProjectDir(home)
            .forwardOutput()
            .withArguments(args.toList())
            .build()
            .apply {
              println("Build output: ${this.output}\n\n")
            }
  }

  @Test
  fun `apply plugin works`() {

    runSuccessfulBuild("tasks").tasks.forEach { println(it.path + ": " + it.outcome) }
    //NOP
  }
/*

  @Test
  fun `exposes task dsl2xml`() {
    Assert.assertTrue(
            executeTest("dsl2xml").build().tasks.any { it.path == ":dsl2xml" }
    )
  }

  @Test
  fun `exposes task xml2dsl`() {
    Assert.assertTrue(
            executeTest("xml2dsl").build().tasks.any { it.path == ":xml2dsl" }
    )
  }
*/
}


class `Gradle001` : PluginTest("gradle-001") {

}