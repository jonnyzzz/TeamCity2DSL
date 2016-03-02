package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.api.metaRunnerMixin
import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.model.TCMetaRunner
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.writeUTF
import java.io.File
import kotlin.collections.forEach


private val TCMetaRunner.className: String
  get() = "Meta_" + id!!

private val TCMetaRunner.variableName: String
  get() = className + ".id"


fun generateMetaRunner(context: GenerationContext, home: File, project : TCProject, runner: TCMetaRunner) {
  val templateId = runner.id ?: throw Error("Meta runner should have an id")

  val mainFile = home / "meta_$templateId.tcdsl.kt"

  mainFile.writeUTF {
    generateKotlinDSL(context.options.packageName, "meta_${templateId}") {

      block("object ${runner.className}") {
        appendln("val id = ${project.nameOrRef(context)}.metaRunner(${runner.id?.quote()})")
        appendln()
        block("val mixin = ${::metaRunnerMixin.name}") {
          setter("name", runner.name)
          setter("description", runner.description)

          val settings = runner.settings
          paramsWithSpec(settings.parameters)

          generateSettings(context, settings) {
            val generateRunner = generateRunners(settings.runners)
            settings.runners?.forEach { generateRunner(it) }
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
