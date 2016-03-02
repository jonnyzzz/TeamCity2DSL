package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.core.StringContains
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files

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
        maven { url "http://dl.bintray.com/jonnyzzz/maven" }
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

    fun assert(action : () -> Unit)
  }

  private fun runSuccessfulBuild(setup : RunSetup.() -> Unit): BuildResult {
    val home = temp.newFolder() / "gradle-test"
    home.mkdirs()

    var build_gradle = scriptHeader
    val args = mutableListOf("--stacktrace")
    val assertTasks = mutableListOf<() -> Unit>()

    object : RunSetup {
      override fun assert(action: () -> Unit) {
        assertTasks.add(action)
      }

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

    val result = GradleRunner.create()
            .withDebug(true)
            .withProjectDir(home)
            .forwardOutput()
            .withArguments(args.toList())
            .build()

    assertTasks.forEach { it() }
    return result
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

  @Test
  fun `it should generate DSL_001`() {
    runSuccessfulBuild {
      val toHome = home / ".teamcity"
      Paths.copyRec(Paths.teamcityProjectTestDataPath("test-001"), toHome)

      Files.walk(toHome.toPath()).forEach {
        println( ".teamcity: $it")
      }

      args("xml2dsl")

      assert {
        val gen = home / "dsl.generated"

        val dslFiles = Files.walk(gen.toPath()).toArray()
        dslFiles.forEach {
          println( "generated: $it")
        }

        Assert.assertTrue( gen.isDirectory && dslFiles.size > 4)
      }
    }
  }

  @Test
  fun `it should generate DSL and kompile_001`() {
    runSuccessfulBuild {
      val toHome = home / ".teamcity"
      Paths.copyRec(Paths.teamcityProjectTestDataPath("test-001"), toHome)

      Files.walk(toHome.toPath()).forEach {
        println( ".teamcity: $it")
      }

      args("xml2dsl", "dsl2xml")

      assert {
        val gen = home / "dsl.generated"

        val dslFiles = Files.walk(gen.toPath()).toArray()
        dslFiles.forEach {
          println( "generated: $it")
        }
        Assert.assertTrue(gen.isDirectory && dslFiles.size > 4)

        val classes = home / "build" / "classes" / "main"
        val classFiles = Files.walk(classes.toPath()).toArray()
        classFiles.forEach {
          println("classes: $it")
        }
        Assert.assertTrue(classes.isDirectory && classFiles.size > 4)
      }
    }
  }
}
