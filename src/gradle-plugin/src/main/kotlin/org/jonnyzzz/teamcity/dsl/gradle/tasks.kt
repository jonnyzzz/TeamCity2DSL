package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.DefaultTask
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
    val extension = project.DSLSettings.toResolvedSettings(project)

    println("Running TeamCity2DSL: ${javaClass.simpleName.toLowerCase()}")
    println("  package:    ${extension.pkg}")
    println("  dslPath:    ${extension.dslPath}")
    println("  xmlPath:    ${extension.xmlPath}")
    println("  extensions: ${extension.plugins}")

    val config = project.configurations.getByName("compile")!!
    URLClassLoader(config.files.map{ it.toURI().toURL() }.toTypedArray(), URLClassLoader(arrayOf(), null)).context {
      //TODO: use API not stdout
      standardOutputCapture.start()
      try {
        executeTaskImpl(this, extension)
      } finally {
        standardOutputCapture.stop()
      }
    }
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

    val sourceSet = project.convention.getPlugin(JavaPluginConvention::class.java)
            .sourceSets
            .getByName("main")

    val classpath = sourceSet.runtimeClasspath.files.filter { it.isDirectory }
            .map { it.toURI().toURL() }
            .toTypedArray()


    sourceSet.java.srcDirs.forEach { println("  DSL Sources: " + it) }
    classpath.forEach { println("  DSL Classpath: " + it) }

    val loader = URLClassLoader(classpath, dslClasses)

    dslClasses.loadClass("org.jonnyzzz.teamcity.dsl.main.DSLRunner")
            .getMethod("generateProjects", File::class.java, String::class.java, ClassLoader::class.java)
            .invoke(null, settings.xmlPath, settings.pkg, loader)
  }
}
