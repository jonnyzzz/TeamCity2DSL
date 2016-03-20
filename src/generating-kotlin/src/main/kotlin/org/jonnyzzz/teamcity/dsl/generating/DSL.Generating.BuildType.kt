package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.api.buildMixin
import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.model.TCBuildType
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.writeUTF
import java.io.File
import kotlin.collections.*


val TCBuildType.className: String
  get() = "Build_" + id

val TCBuildType.variableName: String
  get() = className + ".id"


fun generateBuildType(context: GenerationContext, home: File, project : TCProject, build: TCBuildType) {
  val buildId = build.id

  val mainFile = home / "build_$buildId.tcdsl.kt"

  mainFile.writeUTF {
    generateKotlinDSL(context.options.packageName, "build_$buildId") {

      fun generateTemplateIdRef() : String {
        val templateId = build.templateId ?: return ""

        val baseTemplate = context.findTemplate(templateId)
        if (baseTemplate != null) {
          return " + ${baseTemplate.variableName}"
        }

        return " + UnknownTemplate(${templateId.quote()})"
      }

      block("object ${build.className}") {
        appendln("val id = ${project.nameOrRef(context)}.build(${build.id.quote()})" + generateTemplateIdRef())
        appendln()

        block("val mixin = ${::buildMixin.name}") {
          setter("paused", build.paused)

          setter("name", build.name)
          setter("description", build.description)

          block("parameters") {
            paramsWithSpec(build.parameters)
          }

          generateSettings(context, build) {
            val runners = build.runners?.filter { it.id != null } ?: listOf()
            val order = build.runnersOrder ?: runners.map { it.id }.filterNotNull()
            val idToRunner = runners.map { it.id to it }.toMap()

            val generateRunner = generateRunners(context, runners)
            order.forEach {
              val runner = idToRunner[it]
              if (runner == null) {
                appendln("runnerRef(${it.quote()})")
              } else {
                generateRunner(runner)
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
