package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.model.TCMetaRunner
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.writeUTF
import java.io.File
import kotlin.collections.forEach


private val TCMetaRunner.variableName: String
  get() = "Meta_" + id!!


fun generateMetaRunner(context: GenerationContext, home: File, project : TCProject, runner: TCMetaRunner) {
  val templateId = runner.id ?: throw Error("Meta runner should have an id")

  val mainFile = home / "meta_$templateId.tcdsl.kt"

  mainFile.writeUTF {
    generateKotlinDSL(context.options.packageName, "meta_${templateId}") {
      block("val ${runner.variableName} = ${project.nameOrRef(context)}.metaRunner(${runner.id?.quote()})") {
        setter("name", runner.name)
        setter("description", runner.description)

        val settings = runner.settings
        paramsWithSpec(settings.parameters)

        generateSettings(context, settings) {
          val generateRunner = generateRunners(settings.runners)
          settings.runners?.forEach { generateRunner(it) }
        }
      }
    }
  }
}
