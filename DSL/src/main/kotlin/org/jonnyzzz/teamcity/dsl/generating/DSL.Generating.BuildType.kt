package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.model.TCBuildType
import org.jonnyzzz.teamcity.dsl.model.TCBuildTypeSettings
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.writeUTF
import java.io.File
import kotlin.collections.*


val TCBuildType.variableName: String
  get() = "Build_" + id!!


fun generateBuildType(context: GenerationContext, home: File, project : TCProject, build: TCBuildType) {
  val buildId = build.id ?: throw Error("Build type should have an id")

  val mainFile = home / "build_$buildId.tcdsl.kt"

  mainFile.writeUTF {
    generateKotlinDSL(context.options.packageName) {

      fun generateTemplateIdRef() : String {
        val templateId = build.settings.templateId ?: return ""

        val baseTemplate = context.findTemplate(templateId)
        if (baseTemplate != null) {
          return " + ${baseTemplate.variableName} +"
        }

        return " + UnknownTemplate(${templateId.quote()}) +"
      }

      block("val ${build.variableName} = ${project.nameOrRef(context)}.build(${build.id?.quote()})" + generateTemplateIdRef()) {
        setter("paused", build.paused)

        setter("name", build.name)
        setter("description", build.description)

        val settings = build.settings
        paramsWithSpec(settings.parameters)
        generateSettings(context, settings) {
          generateSettingsRunners(settings)
        }
      }
    }
  }
}

fun KotlinWriter.generateSettingsRunners(settings : TCBuildTypeSettings) {
  val runners = settings.runners?.filter { it.id != null } ?: listOf()
  val order = settings.runnersOrder ?: runners.map { it.id }.filterNotNull()
  val idToRunner = runners.map { it.id to it }.toMap()

  val generateRunner = generateRunners(runners)
  order.forEach {
    val runner = idToRunner[it]
    if (runner == null) {
      appendln("runnerRef(${it.quote()})")
    } else {
      generateRunner(runner)
    }
  }
}
