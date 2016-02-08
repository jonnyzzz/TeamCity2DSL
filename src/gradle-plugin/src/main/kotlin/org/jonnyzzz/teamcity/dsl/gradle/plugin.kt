package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class GeneratorPlugin : Plugin<Project> {
  override fun apply(project: Project?) {
    if (project == null) return

    project.tasks.create("xml2dsl", Xml2Dsl::class.java)
    project.tasks.create("dsl2xml", Dsl2Xml::class.java)
  }
}

open class BaseDSLTask : DefaultTask() {

  @TaskAction
  fun `action!`() {
    println("Hohoho!")
  }
}

open class Xml2Dsl : BaseDSLTask() {

}


open class Dsl2Xml : BaseDSLTask() {

}
