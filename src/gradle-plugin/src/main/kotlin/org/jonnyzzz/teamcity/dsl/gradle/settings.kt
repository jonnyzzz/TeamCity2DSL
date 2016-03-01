package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.jonnyzzz.teamcity.dsl.div
import java.io.File


val Project.DSLSettings: DSLSettings
  get() = with(project.extensions) {
    findByType(DSLSettings::class.java) ?: create("teamcity2dsl", DSLSettings::class.java, project.projectDir)
  }


open class DSLSettings(private val baseDir : File) {
  var `package` : String? = "org.jonnyzzz.teamcity.dsl.generated"

  var dslPath : File? = baseDir / "dsl.generated"
  var xmlPath : File? = baseDir / ".teamcity"


  val toResolvedSettings by lazy {
    fun failTask(message : String) : Throwable {
      throw GradleException(message)
    }

    val xmlRoot = xmlPath ?: throw failTask("TeamCity XML root is not defined")
    val dslRoot = dslPath ?: throw failTask("TeamCity DSL root is not defined")
    val pkg = `package` ?: throw failTask("DSL Generation package is not defined")

    ResolvedDSLSettings(pkg, dslRoot.absoluteFile, xmlRoot.absoluteFile)
  }
}

data class ResolvedDSLSettings(val pkg : String,
                               val dslPath : File,
                               val xmlPath : File)

