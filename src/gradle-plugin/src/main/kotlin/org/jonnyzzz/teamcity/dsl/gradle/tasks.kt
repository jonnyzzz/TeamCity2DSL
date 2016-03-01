package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.jonnyzzz.teamcity.dsl.main.importProjects

abstract class BaseDSLTask : DefaultTask() {
  init {
    group = "TeamCity2DSL"
    outputs.upToDateWhen { false }
  }

  @TaskAction
  fun `action!`() {
    val extension = project.DSLSettings

    println("Running TeamCity2DSL: ${javaClass.simpleName.toLowerCase()}")
    println("  package: ${extension.`package`}")
    println("  dslPath: ${extension.dslPath}")
    println("  xmlPath: ${extension.xmlPath}")

    //TODO: use API not stdout
    standardOutputCapture.start()
    try {
      executeTaskImpl(extension.toResolvedSettings)
    } finally {
      standardOutputCapture.stop()
    }
  }

  protected fun failTask(message : String) : Throwable {
    throw GradleException(message)
  }

  protected abstract fun executeTaskImpl(settings : ResolvedDSLSettings)

}

open class Xml2Dsl : BaseDSLTask() {
  override fun executeTaskImpl(settings: ResolvedDSLSettings) {
    importProjects(settings.xmlPath, settings.pkg, settings.dslPath)
  }
}


open class Dsl2Xml : BaseDSLTask() {
  override fun executeTaskImpl(settings: ResolvedDSLSettings) {
    println("Hohoho!")
  }
}
