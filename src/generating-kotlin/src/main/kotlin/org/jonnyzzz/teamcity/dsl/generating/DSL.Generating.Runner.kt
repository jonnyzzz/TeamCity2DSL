package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.api.*
import org.jonnyzzz.teamcity.dsl.clustering.DSLClusteringGenerator
import org.jonnyzzz.teamcity.dsl.clustering.clusteringRunner
import org.jonnyzzz.teamcity.dsl.extensions.Host
import org.jonnyzzz.teamcity.dsl.lang.api.*
import org.jonnyzzz.teamcity.dsl.model.TCBuildSettings
import org.jonnyzzz.teamcity.dsl.model.TCSettingsRunner
import java.util.*
import kotlin.collections.linkedMapOf

fun KotlinWriter.generateRunners(context: GenerationContext, runners: List<TCSettingsRunner>?): KotlinWriter.(TCSettingsRunner) -> Unit {

  return { d ->
    val item = d

    val generators = mutableListOf<BuildRunnerGeneratorResult>()

    for (ext in Host.runnerExtensions) {
      val runnerContext = object: BuildRunnerExtensionGeneratorContext, ExtensionContext by context {
        override val runner: TCSettingsRunner
          get() = d

        override val selectedGenerators: List<BuildRunnerGeneratorResult>
          get() = generators.toList()
      }

      val generator = ext.generate(runnerContext) ?: continue
      generators += generator
    }

    val blockName = buildString {
      append("${TCBuildSettings::runner.name}(${d.id?.quote()}, ${d.runnerType?.quote()})")

      for (gen in generators) {
        val builder = gen.builder
        val text = " + " + kotlinBlockWriter(builder)

        append(text)
      }

      if (generators.isNotEmpty()) {
        append(" +")
      }
    }

    val allParameters = generators.flatMap { it.parameterNames }.toSet()
    block(blockName) {
      if (item.name != null) setter("name", item.name)

      params(item.parameters?.filter {
        val name = it.name
        name == null || !allParameters.contains(it.name) })
    }
  }
}
