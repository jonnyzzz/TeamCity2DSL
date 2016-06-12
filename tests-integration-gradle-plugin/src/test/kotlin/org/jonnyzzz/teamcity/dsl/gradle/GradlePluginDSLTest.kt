package org.jonnyzzz.teamcity.dsl.gradle

import org.junit.Assert
import org.junit.Test
import org.junit.experimental.categories.Category
import org.w3c.dom.Element
import java.net.URL
import java.nio.file.Files
import javax.xml.parsers.DocumentBuilderFactory

interface GradlePluginDSL

@Category(GradlePluginDSL::class)
class GradlePluginDSLTest {

  @Test
  fun `it should wipe output_001`() {
    runSuccessfulGradleBuild {
      script = """

    plugins {
      id "${TeamCity2DSLPlugin.DSL_PLUGIN_NAME}" version "${TeamCity2DSLPlugin.DSL_PLUGIN_LATEST_PUBLIC_VERSION}"
    }

    """

      val toHome = home / ".teamcity"
      val gen = home / "dsl.generated"

      Paths.copyRec(Paths.teamcityProjectTestDataPath("test-001"), toHome)

      val tcMarker = (toHome / "marker.1").apply { parentFile.mkdirs(); writeText("aaa") }
      val dslMarker = (gen / "marker.2").apply { parentFile.mkdirs(); writeText("bbb") }

      Files.walk(toHome.toPath()).forEach {
        println(".teamcity: $it")
      }

      args("xml2dsl", "dsl2xml")

      assert {
        Assert.assertFalse(tcMarker.exists())
        Assert.assertFalse(dslMarker.exists())
      }
    }
  }

  @Test
  fun `include_commandline_plugin_via_DSL_002`() {
    runSuccessfulGradleBuild {
      script = """

    plugins {
      id "${TeamCity2DSLPlugin.DSL_PLUGIN_NAME}" version "${TeamCity2DSLPlugin.DSL_PLUGIN_LATEST_PUBLIC_VERSION}"
    }

    teamcity2dsl {
      extension 'org.jonnyzzz.teamcity.dsl.special:commandline-runner:${TeamCity2DSLPlugin.DSL_PLUGIN_LATEST_PUBLIC_VERSION}'
    }

    """

      val toHome = home / ".teamcity"
      val gen = home / "dsl.generated"

      Paths.copyRec(Paths.teamcityProjectTestDataPath("test-012"), toHome)


      Files.walk(toHome.toPath()).forEach {
        println(".teamcity: $it")
      }

      args("xml2dsl", "dsl2xml")

      assert {
        Files.walk(gen.toPath()).forEach {
          println("gen: $it")
        }

        val text = (gen / "commandline/build_Jonnyzzz_Test.tcdsl.kt").readText()
        println(text)

        Assert.assertTrue(text.contains("script {"))
        Assert.assertTrue(text.contains("+ \"echo 239\""))
      }
    }
  }
}
