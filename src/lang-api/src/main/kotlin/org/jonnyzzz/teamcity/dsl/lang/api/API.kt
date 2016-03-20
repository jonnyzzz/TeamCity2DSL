package org.jonnyzzz.teamcity.dsl.lang.api

import org.jonnyzzz.teamcity.dsl.generating.KotlinMixinkWriter
import org.jonnyzzz.teamcity.dsl.model.*


interface ExtensionPriority {
  val priority : Double
}

interface ExtensionContext {
  val version : TeamCityVersion
  val projects: List<TCProject>
}

interface ExtensionRunnerContext : ExtensionContext {
  val runner : TCSettingsRunner
  val selectedGenerators : List<BuildRunnerGeneratorResult>
}

interface BuildRunnerGeneratorContext : ExtensionRunnerContext {

  //TODO: UserDataHolder?
}

interface BuildRunnerExtensionGeneratorContext : ExtensionRunnerContext {
  //TODO: UserDataHolder?
}

class BuildRunnerGeneratorResult(
        val parameterNames: Set<String>,
        val builder: KotlinMixinkWriter.() -> Unit
)

abstract class BuildRunnerGenerator : ExtensionPriority {
  abstract fun generate(context : BuildRunnerGeneratorContext) : BuildRunnerGeneratorResult?
}

abstract class BuildRunnerExtensionGenerator : ExtensionPriority {
  abstract fun generate(context : BuildRunnerExtensionGeneratorContext) : BuildRunnerGeneratorResult?
}

