package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.api.templateMixin
import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.model.TCBuildTemplate
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.writeUTF
import java.io.File


val TCBuildTemplate.className: String
  get() = "Template_" + id

val TCBuildTemplate.variableName: String
  get() = className + ".id"

fun generateTemplate(context: GenerationContext, home: File, project : TCProject, template: TCBuildTemplate) {
  val templateId = template.id

  val mainFile = home / "template_$templateId.tcdsl.kt"

  mainFile.writeUTF {
    generateKotlinDSL(context.options.packageName, "template_$templateId") {

      block("object ${template.className}") {
        appendln("val id = ${project.nameOrRef(context)}.template(${template.id.quote()})")
        appendln()
        block("val mixin = ${::templateMixin.name}") {
          setter("name", template.name)
          setter("description", template.description)

          block("parameters") {
            paramsWithSpec(template.parameters)
          }

          generateSettings(context, template) {
            val generateRunner = generateRunners(template.runners)
            template.runners?.forEach { generateRunner(it) }
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
