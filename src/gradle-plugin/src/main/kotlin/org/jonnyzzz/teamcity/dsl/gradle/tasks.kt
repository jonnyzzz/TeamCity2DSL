package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URLClassLoader

abstract class BaseDSLTask : DefaultTask() {
  init {
    group = "TeamCity2DSL"
    outputs.upToDateWhen { false }
  }

  protected val dslClasses by lazy {
    val config = project.configurations.getByName(TEAMCITY_RUNNER_CONFIGURATION) ?: throw failTask("Failed to find internal configuration")
    URLClassLoader(config.files.map{ it.toURI().toURL() }.toTypedArray(), URLClassLoader(arrayOf(), null))
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
    dslClasses.context {
      loadClass("org.jonnyzzz.teamcity.dsl.main.DSLRunner")
              .getMethod("importProjects", File::class.java, String::class.java, File::class.java)
              .invoke(null, settings.xmlPath, settings.pkg, settings.dslPath)
    }
  }
}

open class Dsl2Xml : BaseDSLTask() {
  override fun executeTaskImpl(settings: ResolvedDSLSettings) {
    println("Hohoho!")
  }
}
