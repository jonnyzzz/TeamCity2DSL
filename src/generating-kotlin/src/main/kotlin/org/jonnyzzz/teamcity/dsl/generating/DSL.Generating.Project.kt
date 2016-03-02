package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.api.projectMixin
import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.model.isEmpty
import org.jonnyzzz.teamcity.dsl.writeUTF
import java.io.File
import kotlin.collections.forEach

val TCProject.className: String
  get() = "Project_" + id!!

val TCProject.variableName: String
  get() = className + ".id"

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

      fun generateProjectRef(parentId : String) : String {
        val base = context.findProject(parentId)
        if (base != null) return "${base.variableName}"

        return "UnknownProject(${parentId.quote()})"
      }

      fun generateParentProjectRef() : String {
        val parentId = project.parentId ?: return "RootProject"
        return generateProjectRef(parentId)
      }

      block("object ${project.className}") {
        appendln("val id = ${generateParentProjectRef()}.project(${project.id?.quote()})")
        appendln()

        block("val mixin = ${::projectMixin.name}") {
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

          val ordering = project.ordering
          if (!ordering.isEmpty()) {

            block("ordering") {
              ordering?.projectsOrder?.forEach {
                appendln("+ ${ context.findProject( it )!!.variableName }")
              }

              appendln()

              ordering?.buildsOrder?.forEach {
                appendln("+ ${ context.findBuild( it )!!.variableName }")
              }
            }
          }
        }

        appendln()
        block("init") {
          appendln("id += mixin")
        }
      }
    }
  }
}
