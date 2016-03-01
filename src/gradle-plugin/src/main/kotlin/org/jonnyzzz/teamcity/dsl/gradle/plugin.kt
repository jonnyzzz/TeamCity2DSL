package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention

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
      add("compile", "org.jonnyzzz.teamcity.dsl:DSL:SNAPSHOT") //TODO: use API module here!
    }

    val settings = project.DSLSettings

    project.tasks.create("xml2dsl", Xml2Dsl::class.java)

    val dsl2xml = project.tasks.create("dsl2xml", Dsl2Xml::class.java)
    dsl2xml.dependsOn(project.tasks.getByName("classes"))

    project.afterEvaluate {
      println("Adding DSL path to Kotlin source set: ${settings.dslPath}")
      project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName("main").java.srcDir( settings.dslPath )
    }
  }
}
