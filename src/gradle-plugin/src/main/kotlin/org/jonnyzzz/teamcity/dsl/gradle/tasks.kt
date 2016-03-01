package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URLClassLoader

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

    val config = project.configurations.getByName(TEAMCITY_RUNNER_CONFIGURATION) ?: throw failTask("Failed to find internal configuration")
    URLClassLoader(config.files.map{ it.toURI().toURL() }.toTypedArray(), URLClassLoader(arrayOf(), null)).context {
      //TODO: use API not stdout
      standardOutputCapture.start()
      try {
        executeTaskImpl(this, extension.toResolvedSettings)
      } finally {
        standardOutputCapture.stop()
      }
    }
  }

  protected fun failTask(message : String) : Throwable {
    throw GradleException(message)
  }

  protected abstract fun executeTaskImpl(dslClasses : ClassLoader, settings : ResolvedDSLSettings)
}

open class Xml2Dsl : BaseDSLTask() {
  override fun executeTaskImpl(dslClasses : ClassLoader, settings: ResolvedDSLSettings) {
    dslClasses.loadClass("org.jonnyzzz.teamcity.dsl.main.DSLRunner")
            .getMethod("importProjects", File::class.java, String::class.java, File::class.java)
            .invoke(null, settings.xmlPath, settings.pkg, settings.dslPath)
  }
}

open class Dsl2Xml : BaseDSLTask() {
  override fun executeTaskImpl(dslClasses : ClassLoader, settings: ResolvedDSLSettings) {

    val classpath = project.convention.getPlugin(JavaPluginConvention::class.java)
            .sourceSets
            .getByName("main")
            .output
            .dirs
            .map { it.toURI().toURL() }
            .toTypedArray()


    val loader = URLClassLoader(classpath, dslClasses)

    dslClasses.loadClass("org.jonnyzzz.teamcity.dsl.main.DSLRunner")
            .getMethod("generateProjects", File::class.java, String::class.java, ClassLoader::class.java)
            .invoke(null, settings.xmlPath, settings.pkg, loader)
  }
}
