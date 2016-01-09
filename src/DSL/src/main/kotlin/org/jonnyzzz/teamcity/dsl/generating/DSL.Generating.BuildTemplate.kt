package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.model.TCBuildTemplate
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.writeUTF
import java.io.File
import kotlin.collections.forEach


val TCBuildTemplate.variableName: String
  get() = "Template_" + id!!


fun generateTemplate(context: GenerationContext, home: File, project : TCProject, template: TCBuildTemplate) {
  val templateId = template.id ?: throw Error("Build template should have an id")

  val mainFile = home / "template_$templateId.tcdsl.kt"

  mainFile.writeUTF {
    generateKotlinDSL(context.options.packageName) {
      block("val ${template.variableName} = ${project.nameOrRef(context)}.template(${template.id?.quote()})") {
        setter("name", template.name)
        setter("description", template.description)

        val settings = template.settings
        paramsWithSpec(settings.parameters)

        generateSettings(context, settings) {
          val generateRunner = generateRunners(settings.runners)
          settings.runners?.forEach { generateRunner(it) }
        }
      }
    }
  }
}
