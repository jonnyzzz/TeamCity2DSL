package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.TCSettingsOptions
import org.jonnyzzz.teamcity.dsl.model.TCWithSettings


interface TCSettingsOptionsMixin {
  fun asBuilder(): TCSettingsOptions.() -> Unit
}

interface TCSettingsOptionsMixinBuilder : TCSettingsOptionsMixin {
  operator fun plus(mixin: TCSettingsOptionsMixin): TCSettingsOptionsMixinBuilder = this + mixin.asBuilder()
  operator fun plus(builder: TCSettingsOptions.() -> Unit): TCSettingsOptionsMixinBuilder
}

interface TCSettingsOptionsBuilder {
  operator fun plus(mixin: TCSettingsOptionsMixin): TCSettingsOptionsBuilder = this + mixin.asBuilder()
  operator fun plus(builder: TCSettingsOptions.() -> Unit): TCSettingsOptionsBuilder
}

fun optionsMixin(builder: TCSettingsOptions.() -> Unit = {}): TCSettingsOptionsMixinBuilder {
  class TCSettingsOptionsMixinBuilderImpl(val actions: List<TCSettingsOptions.() -> Unit> = listOf()) : TCSettingsOptionsMixinBuilder {
    override fun asBuilder(): TCSettingsOptions.() -> Unit = { actions.forEach { it() } }
    override fun plus(builder: TCSettingsOptions.() -> Unit) = TCSettingsOptionsMixinBuilderImpl(actions + builder)
  }
  return TCSettingsOptionsMixinBuilderImpl() + builder
}

fun TCWithSettings.options(builder : TCSettingsOptions.() -> Unit = {}) : TCSettingsOptionsBuilder {
  val opts = settings.options ?: TCSettingsOptions()
  settings.options = opts

  return object:TCSettingsOptionsBuilder {
    operator override fun plus(builder: TCSettingsOptions.() -> Unit): TCSettingsOptionsBuilder = this.apply { opts.builder() }
  } + builder
}

val BuildOptions_ExecutionTimeout : String = "executionTimeoutMin"
val BuildOptions_ArtifactRules : String = "artifactRules"
val BuildOptions_BuildNumberPattern: String = "buildNumberPattern"
val BuildOptions_MaximumNumberOfBuilds: String = "maximumNumberOfBuilds"
val BuildOptions_CheckoutMode: String = "checkoutMode"
val BuildOptions_CheckoutMode_ON_AGENT: String = "ON_AGENT"
val BuildOptions_CleanBuild: String = "cleanBuild"

fun TCSettingsOptions.agentSideCheckout() {
  option(BuildOptions_CheckoutMode, BuildOptions_CheckoutMode_ON_AGENT)
}
fun TCSettingsOptions.cleanSources() {
  option(BuildOptions_CleanBuild, "true")
}

var TCSettingsOptions.executionTimeout: Long?
  get() {
    val timeout = options?.firstOrNull { it.name == BuildOptions_ExecutionTimeout }?.value ?: return null
    try {
      return timeout.toLong()
    } catch (t: Throwable) {
      //NOP
    }
    return null
  }
  set(v: Long?) {
    if (v == null) {
      options = options?.filter { it.name != BuildOptions_ExecutionTimeout }
    } else {
      option(BuildOptions_ExecutionTimeout, v.toString())
    }
  }

var TCSettingsOptions.maximumParallelBuilds: Int?
  get() {
    val timeout = options?.firstOrNull { it.name == BuildOptions_MaximumNumberOfBuilds }?.value ?: return null
    try {
      return timeout.toInt()
    } catch (t: Throwable) {
      //NOP
    }
    return null
  }
  set(v: Int?) {
    if (v == null) {
      options = options?.filter { it.name != BuildOptions_MaximumNumberOfBuilds }
    } else {
      option(BuildOptions_MaximumNumberOfBuilds, v.toString())
    }
  }

interface TCSettingsOptionsArtifactsBuilder {
  operator fun String.unaryPlus() : TCSettingsOptionsArtifactsBuilderDestination
}

interface TCSettingsOptionsArtifactsBuilderDestination {
  infix fun to(destination : String)
}

fun TCSettingsOptions.publishArtifacts(builder: TCSettingsOptionsArtifactsBuilder.() -> Unit) {
  val paths = StringBuilder()
  object : TCSettingsOptionsArtifactsBuilder, TCSettingsOptionsArtifactsBuilderDestination {
    private val that = this

    override fun String.unaryPlus() : TCSettingsOptionsArtifactsBuilderDestination {
      if (paths.length > 0) paths.append("\n")
      paths.append(this)
      return that
    }

    override fun to(destination: String) {
      paths.append(" => ").append(destination)
    }
  }.builder()

  option(BuildOptions_ArtifactRules, paths.toString())
}

var TCSettingsOptions.buildNumberPattern: String?
  get() = options?.firstOrNull { it.name == BuildOptions_BuildNumberPattern }?.value
  set(v: String?) {
    if (v == null) {
      options = options?.filter { it.name != BuildOptions_BuildNumberPattern }
    } else {
      option(BuildOptions_BuildNumberPattern, v.toString())
    }
  }

