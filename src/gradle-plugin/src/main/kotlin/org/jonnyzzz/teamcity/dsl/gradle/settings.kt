package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.api.Project
import java.io.File
import java.util.*


val Project.DSLSettings: DSLSettings
  get() = project.extensions.getByType(DSLSettings::class.java)!!

interface ExtensionHandler {
  fun extension(x: Any)
}

open class DSLSettings(private val extensionHandler: ExtensionHandler) : ExtensionHandler by extensionHandler {
  var targetPackage = "org.jonnyzzz.teamcity.dsl_generated"
  var dslPath = "dsl.generated"
  var xmlPath = ".teamcity"
  var extensions = ArrayList<Any?>()
}


fun DSLSettings.toResolvedSettings(p: Project): ResolvedDSLSettings {
  val xmlRoot = p.file(xmlPath)
  val dslRoot = p.file(dslPath)
  val pkg = targetPackage

  return ResolvedDSLSettings(extensions.filterNotNull().toList(), pkg, dslRoot.absoluteFile, xmlRoot.absoluteFile)
}

data class ResolvedDSLSettings(val plugins : List<Any>,
                               val pkg : String,
                               val dslPath : File,
                               val xmlPath : File)

