package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.writeUTF
import java.io.File
import kotlin.collections.forEach

val TCProject.variableName: String
  get() = "Project_" + id!!

fun TCProject.nameOrRef(context: GenerationContext): String {
  return when (context.isDeclared(this)) {
    true -> variableName
    else -> throw RuntimeException("Project $id is referred but not declared")
  }
}

fun generateProject(context: GenerationContext, home: File, project: TCProject) {
  val projectId = project.id!!

  (home / "project_$projectId.tcdsl.kt").writeUTF {
    generateKotlinDSL(context.options.packageName, "project_$projectId") {

      fun generateParentProjectRef() : String {
        val parentId = project.parentId ?: return "RootProject"

        val base = context.findProject(parentId)
        if (base != null) return "${base.variableName}"

        return "UnknownProject(${parentId.quote()})"
      }

      block("val ${project.variableName} = ${generateParentProjectRef()}.project(${project.id?.quote()})") {
        setter("archived", project.archived)
        setter("name", project.name)
        setter("description", project.description)

        paramsWithSpec(project.parameters)

        val cleanup = project.cleanup
        if (cleanup != null) {
          block("cleanup") {
            elementInternals(cleanup)
          }
        }

        val plugins = project.pluginSettings
        if (plugins != null) {
          block("plugins") {
            plugins.settings?.forEach {
              element(it)
            }
          }
        }
      }
    }
  }
}
