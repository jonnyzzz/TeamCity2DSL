package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.core.StringContains
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class IntegrationPluginTest {
  @Rule
  @JvmField()
  val temp = TemporaryFolder()

  val scriptHeader by lazy {
    val DSL_PLUGIN_CLASSPATH = System.getProperty("TEST_PLUGIN_CLASSPATH")!!
    val DSL_PLUGIN_NAME = System.getProperty("TEST_PLUGIN_NAME")!!
    """
    buildscript {

      repositories {
        mavenLocal()
        mavenCentral()
      }

      dependencies {
        classpath '${DSL_PLUGIN_CLASSPATH}'
      }
    }


    apply plugin: '${DSL_PLUGIN_NAME}'

   """
  }

  interface RunSetup {
    val home : File
    fun args(vararg s : String)
  }

  private fun runSuccessfulBuild(setup : RunSetup.() -> Unit): BuildResult {
    val home = temp.newFolder() / "gradle-test"
    home.mkdirs()

    var build_gradle = scriptHeader
    val args = mutableListOf<String>()

    object : RunSetup {
      override val home: File
        get() = home

      override fun args(vararg s: String) {
        args.addAll(s)
      }
    }.setup()

    (home / "build.gradle").apply {
      println("Patched project script:\n$build_gradle\n")
      writeText(build_gradle)
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
    val text =
    runSuccessfulBuild {
      args("tasks")
    }.output

    Assert.assertThat(text, StringContains.containsString("TeamCity2DSL"))
    Assert.assertThat(text, StringContains.containsString("dsl2xml"))
    Assert.assertThat(text, StringContains.containsString("xml2dsl"))
  }
}
