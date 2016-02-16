package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.jonnyzzz.teamcity.dsl.div
import java.io.File

class GeneratorPlugin : Plugin<Project> {
  override fun apply(project: Project?) {
    if (project == null) return

/*
    project.apply { config ->
      config.plugin("java")
      config.plugin("kotlin")
    }
*/

    project.extensions.create("teamcity2dsl", DSLSettings::class.java, project.buildDir)

    project.tasks.create("xml2dsl", Xml2Dsl::class.java)
    project.tasks.create("dsl2xml", Dsl2Xml::class.java)
  }
}

open class DSLSettings(private val baseDir : File) {
  var `package` : String? = "org.jonnyzzz.teamcity.dsl.generated"

  var dslPath : File? = baseDir / "dsl.generated"
  var xmlPath : File? = baseDir / ".teamcity"
}

open class BaseDSLTask : DefaultTask() {
  init {
    group = "TeamCity2DSL"
    outputs.upToDateWhen { false }
  }
}

open class Xml2Dsl : BaseDSLTask() {
  @TaskAction
  fun `action!`() {
    println("Hohoho!")
  }
}


open class Dsl2Xml : BaseDSLTask() {
  @TaskAction
  fun `action!`() {
    println("Hohoho!")
  }
}
