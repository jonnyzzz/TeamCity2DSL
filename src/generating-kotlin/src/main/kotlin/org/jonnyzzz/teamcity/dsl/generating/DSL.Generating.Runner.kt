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

    val generators = mutableListOf<BuildRunnerExtensionGeneratorResult>()

    for (ext in Host.runnerExtensions) {
      val runnerContext = runnerExtensionContext(context, d, generators)

      val generator = ext.generate(runnerContext) ?: continue
      generators += generator
    }

    val mixins = buildString {
      for (gen in generators) {
        val builder = gen.builder
        val text = " + " + kotlinBlockWriter(builder)

        append(text)
      }

      if (generators.isNotEmpty()) {
        append(" +")
      }
    }

    val runnerGenerator = Host.runners.asSequence().map { it.generate(runnerContext(context, d, generators)) }.filterNotNull().firstOrNull()

    val blockName = when {
      runnerGenerator == null -> "${TCBuildSettings::runner.name}(${d.id?.quote()}, ${d.runnerType?.quote()})"
      else -> "${TCBuildSettings::runner.name}(${d.id?.quote()})"
    } + mixins

    block(blockName) {
      if (item.name != null) setter("name", item.name)

      runnerGenerator?.apply { builder() }

      val allParameters = generators.flatMap { it.parameterNames }.toSet() + (runnerGenerator?.parameterNames ?: setOf())
      params(item.parameters?.filter {
        val name = it.name
        name == null || !allParameters.contains(it.name) })
    }
  }
}

private fun runnerExtensionContext(context: GenerationContext, d: TCSettingsRunner, generators: MutableList<BuildRunnerExtensionGeneratorResult>): BuildRunnerExtensionGeneratorContext {
  return object : BuildRunnerExtensionGeneratorContext, ExtensionContext by context {
    override val runner: TCSettingsRunner
      get() = d

    override val selectedGenerators: List<BuildRunnerExtensionGeneratorResult>
      get() = generators.toList()
  }
}

private fun runnerContext(context: GenerationContext, d: TCSettingsRunner, generators: MutableList<BuildRunnerExtensionGeneratorResult>): BuildRunnerGeneratorContext {
  return object : BuildRunnerGeneratorContext, ExtensionContext by context {
    override val runner: TCSettingsRunner
      get() = d

    override val selectedGenerators: List<BuildRunnerExtensionGeneratorResult>
      get() = generators.toList()
  }
}
