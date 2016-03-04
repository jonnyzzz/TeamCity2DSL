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

          generateOrdering(context, project)
        }

        appendln()
        block("init") {
          appendln("id += mixin")
        }
      }
    }
  }
}

fun KotlinWriter.generateOrdering(context: GenerationContext, project : TCProject) {
  val ordering = project.ordering
  if (ordering.isEmpty()) return

  block("ordering") {
    val projectsOrder = ordering?.projectsOrder
    val buildsOrder = ordering?.buildsOrder

    projectsOrder?.forEach {
      appendln("+ ${ context.findProject( it )!!.variableName }")
    }

    if (projectsOrder?.any() ?: false && buildsOrder?.any() ?: false) appendln()

    buildsOrder?.forEach {
      appendln("+ ${ context.findBuild( it )!!.variableName }")
    }
  }
}
