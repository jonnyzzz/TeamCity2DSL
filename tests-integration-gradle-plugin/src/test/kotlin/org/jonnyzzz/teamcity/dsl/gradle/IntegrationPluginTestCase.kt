package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.testkit.runner.BuildResult
import org.junit.Rule
import org.junit.rules.TemporaryFolder

open class IntegrationPluginTestCase {
  @Rule
  @JvmField()
  val temp = TemporaryFolder()


  protected fun runSuccessfulBuild(setup: RunSetup.() -> Unit): BuildResult = runSuccessfulGradleBuild {
    script = TeamCity2DSLPlugin.scriptHeader
    setup()
  }

  val TeamCity2DSLPlugin.scriptHeader by lazy {
    """
    buildscript {
      repositories {
        mavenLocal()
        mavenCentral()
        maven { url "https://plugins.gradle.org/m2/" }
      }

      dependencies {
        classpath '${TeamCity2DSLPlugin.DSL_PLUGIN_CLASSPATH}:${TeamCity2DSLPlugin.DSL_PLUGIN_VERSION}'
      }
    }

    apply plugin: '${TeamCity2DSLPlugin.DSL_PLUGIN_NAME}'

   """
  }
}
