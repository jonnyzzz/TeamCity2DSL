package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.jonnyzzz.teamcity.dsl.gradle.generated.GradlePluginBuildConstants

val TEAMCITY_RUNNER_CONFIGURATION = "teamcity_runner"


class GeneratorPlugin : Plugin<Project> {
  override fun apply(project: Project?) {
    if (project == null) return

    project.apply { config ->
      config.plugin("java")
      config.plugin("kotlin")
    }

    //TODO: include means to setup maven proxy for those repositories
    project.repositories.apply {
      jcenter()
      mavenCentral()
      maven { it.setUrl("http://dl.bintray.com/jonnyzzz/maven") }
      mavenLocal()
    }

    val configuration = project.configurations.maybeCreate(TEAMCITY_RUNNER_CONFIGURATION).setVisible(false).setTransitive(true)
    project.dependencies.apply {
      val reference = "${GradlePluginBuildConstants.group}:${GradlePluginBuildConstants.name_DSL}:${GradlePluginBuildConstants.version}"

      add(configuration.name, reference)
      add("compile", reference) //TODO: use API module here!
    }

    val settings = project.DSLSettings

    project.tasks.create("xml2dsl", Xml2Dsl::class.java)

    val dsl2xml = project.tasks.create("dsl2xml", Dsl2Xml::class.java)
    dsl2xml.dependsOn(project.tasks.getByName("classes"))

    println("Adding DSL path to Kotlin source set: ${settings.dslPath}")
    val sourceSets = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets

    println("Source sets: ${sourceSets.names}")
    sourceSets.getByName("main").java.srcDir( settings.dslPath!!.path )
  }
}
