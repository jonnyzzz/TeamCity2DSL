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
}
