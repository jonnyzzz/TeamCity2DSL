package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object Paths {
  fun path(name : String) : File =
          System.getProperty("TEST_DATA_HOME").apply {
            Assert.assertNotNull("Test data dir property should be set", this)
          }.let {

            println(it)
            File(it).apply {
              Assert.assertTrue("Test data dir $this should exist", exists())
            }
          }.let {
            File(it, name).apply {
              Assert.assertTrue("File $this should exist", exists())
            }.canonicalFile
          }
}

abstract class PluginTest(val testName : String) {
  lateinit var home : File
  lateinit var project : Project

  @Rule
  @JvmField()
  val temp = TemporaryFolder()


  @Before
  fun setup() {
    home = temp.newFolder()

    Files.copy(Paths.path(testName).toPath(), home.toPath(), StandardCopyOption.REPLACE_EXISTING)

    project = ProjectBuilder.builder().withName(testName).withProjectDir(home).build()
    project.extensions.add("ext.DSL_PLUGIN_CLASSPATH", System.getProperty("TEST_PLUGIN_NAME"))
    project.extensions.add("ext.DSL_PLUGIN_NAME", "WTF")
  }

  @Test
  fun `apply plugin works`() {
    //NOP
  }

  @Test
  fun `exposes task dsl2xml`() {
project.afterEvaluate {  }
    Assert.assertTrue(
            project.tasks.any { it.name == "dsl2xml"}
    )
  }

  @Test
  fun `exposes task xml2dsl`() {
    Assert.assertTrue(
            project.tasks.any { it.name == "xml2dsl" }
    )
  }
}


abstract class `Gradle001` : PluginTest("gradle-001") {

}