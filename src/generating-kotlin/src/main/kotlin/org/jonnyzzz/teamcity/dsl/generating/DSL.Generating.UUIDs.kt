package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.api.TCUUIDs
import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.writeUTF
import java.io.File
import kotlin.collections.filter
import kotlin.collections.flatMap
import kotlin.collections.forEach
import kotlin.collections.sortedBy
import kotlin.text.toLowerCase


fun generateUUIDsMap(context : GenerationContext, root : File, projects : List<TCProject>, options : DSLOptions) {
  val allProjects = projects
  val allVCSRoots = projects.flatMap { it.vcsRoots }
  val allBuilds = projects.flatMap { it.buildTypes }
  val allTemplates = projects.flatMap { it.buildTemplates }

  if (allProjects.isEmpty() && allVCSRoots.isEmpty() && allBuilds.isEmpty() && allTemplates.isEmpty()) return

  root.mkdirs()
  (root / "uuids.kt").writeUTF {
    generateKotlinDSL(options.packageName, "uuids") {
      block2("object UUIDs : ${TCUUIDs::class.java.getSimpleName()}()") {

        appendln()
        allProjects.filter { it.uuid != null }.sortedBy { it.variableName.toLowerCase() }.forEach { appendln("${it.variableName} += ${it.uuid!!.quote()}") }

        appendln()
        allVCSRoots.filter { it.uuid != null }.sortedBy { it.variableName.toLowerCase() }.forEach { appendln("${it.variableName} += ${it.uuid!!.quote()}") }

        appendln()
        allBuilds.filter { it.uuid != null }.sortedBy { it.variableName.toLowerCase() }.forEach { appendln("${it.variableName} += ${it.uuid!!.quote()}") }

        appendln()
        allTemplates.filter { it.uuid != null }.sortedBy { it.variableName.toLowerCase() }.forEach { appendln("${it.variableName} += ${it.uuid!!.quote()}") }
      }
    }
  }
}
