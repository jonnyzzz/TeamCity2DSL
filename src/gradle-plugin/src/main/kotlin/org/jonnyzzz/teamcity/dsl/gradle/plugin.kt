package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.jonnyzzz.teamcity.dsl.gradle.generated.GradlePluginBuildConstants


class GeneratorPlugin : Plugin<Project> {
  override fun apply(project: Project?) {
    if (project == null) return

    val extensionHandler = object:ExtensionHandler {
      override fun extension(x: Any) {
        project.dependencies.add("compile", x)
      }
    }

    project.extensions.create("teamcity2dsl", DSLSettings::class.java, extensionHandler)

    project.apply { config ->
      config.plugin("java")
      config.plugin("kotlin")
    }

    //TODO: include means to setup maven proxy for those repositories
    project.repositories.apply {
      jcenter()
      mavenCentral()
      maven { it.setUrl("http://dl.bintray.com/jonnyzzz/maven") }

      if (GradlePluginBuildConstants.version.contains("SNAPSHOT")) {
        mavenLocal()
      }
    }

    //TODO: split API & generator dependencies here!
    extensionHandler.extension("${GradlePluginBuildConstants.group}:${GradlePluginBuildConstants.name_DSL}:${GradlePluginBuildConstants.version}")

    project.tasks.create("xml2dsl", Xml2Dsl::class.java)

    val dsl2xml = project.tasks.create("dsl2xml", Dsl2Xml::class.java)
    dsl2xml.dependsOn(project.tasks.getByName("classes"))

    val settings = project.DSLSettings.toResolvedSettings(project)

    println("Adding DSL path to Kotlin source set: ${settings.dslPath}")
    val sourceSets = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets

    println("Source sets: ${sourceSets.names}")
    sourceSets.getByName("main").java.srcDir( settings.dslPath.path )
  }
}
