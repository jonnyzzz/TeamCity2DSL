package org.jonnyzzz.teamcity.dsl.gradle

import org.hamcrest.core.StringContains
import org.junit.Assert
import org.junit.Test
import java.nio.file.Files

class IntegrationPluginTest : IntegrationPluginTestCase() {

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

  @Test
  fun `it should wipe output_001`() {
    runSuccessfulBuild {
      val toHome = home / ".teamcity"
      val gen = home / "dsl.generated"

      Paths.copyRec(Paths.teamcityProjectTestDataPath("test-001"), toHome)

      val tcMarker = (toHome / "marker.1").apply { parentFile.mkdirs(); writeText("aaa") }
      val dslMarker = (gen / "marker.2").apply { parentFile.mkdirs(); writeText("bbb") }

      Files.walk(toHome.toPath()).forEach {
        println( ".teamcity: $it")
      }

      args("xml2dsl", "dsl2xml")

      assert {
        Assert.assertFalse(tcMarker.exists())
        Assert.assertFalse(dslMarker.exists())
      }
    }
  }
}
