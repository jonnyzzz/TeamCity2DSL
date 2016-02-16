package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class PluginTest {
  lateinit var project : Project

  @Before
  fun setup() {
    project = ProjectBuilder.builder().build()
    project.plugins.apply(GeneratorPlugin::class.java)
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
