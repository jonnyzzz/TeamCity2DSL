package org.jonnyzzz.teamcity.dsl.gradle

import org.junit.Assert
import org.junit.Test
import java.nio.file.Files


class PluginDSLTest {

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
      addx22 "org.jonnyzzz.teamcity.dsl.special:commandline-runner:${TeamCity2DSLPlugin.DSL_PLUGIN_LATEST_PUBLIC_VERSION}"
    }

    """

      val toHome = home / ".teamcity"
      val gen = home / "dsl.generated"

      Paths.copyRec(Paths.teamcityProjectTestDataPath("test-002"), toHome)

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
}
