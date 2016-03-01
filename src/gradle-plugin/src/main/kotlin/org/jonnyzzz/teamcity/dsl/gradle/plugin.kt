package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.tooling.BuildException
import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.main.importProjects
import java.io.File

class GeneratorPlugin : Plugin<Project> {
  override fun apply(project: Project?) {
    if (project == null) return

    project.apply { config ->
      config.plugin("java")
      config.plugin("kotlin")
    }

    val x = project.DSLSettings
    x.toString()

    project.tasks.create("xml2dsl", Xml2Dsl::class.java)
    project.tasks.create("dsl2xml", Dsl2Xml::class.java)
  }
}
