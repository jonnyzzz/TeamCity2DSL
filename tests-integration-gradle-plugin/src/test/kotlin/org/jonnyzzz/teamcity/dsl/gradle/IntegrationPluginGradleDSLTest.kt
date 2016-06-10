package org.jonnyzzz.teamcity.dsl.gradle

import org.junit.Assert
import org.junit.Test
import java.nio.file.Files

class IntegrationPluginGradleDSLTest : IntegrationPluginTestCase() {

  @Test
  fun `include_commandline_plugin_via_DSL_012`() {
    runSuccessfulBuild {
      script += """

      // debugging

      println()
      org.jonnyzzz.teamcity.dsl.gradle.DSLSettings.class.getMethods().each { method ->
        println method.name
      }
      println()


    teamcity2dsl {
      extension 'org.jonnyzzz.teamcity.dsl.special:commandline-runner:${TeamCity2DSLPlugin.DSL_PLUGIN_VERSION}'
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