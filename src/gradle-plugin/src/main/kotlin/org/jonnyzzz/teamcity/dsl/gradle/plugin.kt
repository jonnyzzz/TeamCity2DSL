package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

val TEAMCITY_RUNNER_CONFIGURATION = "teamcity_runner"


class GeneratorPlugin : Plugin<Project> {
  override fun apply(project: Project?) {
    if (project == null) return

    project.apply { config ->
      config.plugin("java")
      config.plugin("kotlin")
    }

    val renamer = project.configurations.maybeCreate(TEAMCITY_RUNNER_CONFIGURATION).setVisible(false).setTransitive(true)

    project.dependencies.apply {
      add(renamer.name, "org.jonnyzzz.teamcity.dsl:DSL:SNAPSHOT") //TODO: generate version and names from gradle build
    }

    val x = project.DSLSettings
    x.toString()

    project.tasks.create("xml2dsl", Xml2Dsl::class.java)
    project.tasks.create("dsl2xml", Dsl2Xml::class.java)
  }
}
