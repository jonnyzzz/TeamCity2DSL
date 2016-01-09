package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class GeneratorPlugin : Plugin<Project> {
  override fun apply(target: Project?) {

  }
}

class DSLGeneratorTask : DefaultTask() {

  @TaskAction
  fun `action!`() {
    println("Hohoho!")
  }

}
