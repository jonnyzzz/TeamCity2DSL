package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.having
import org.jonnyzzz.teamcity.dsl.model.TCSettingsRunner
import org.jonnyzzz.teamcity.dsl.model.TCSettingsRunnerRef
import org.jonnyzzz.teamcity.dsl.model.TCWithSettings

interface TCRunnerMixin {
  fun asBuilder(): TCSettingsRunner.() -> Unit
}

interface TCRunnerMixinBuilder : TCRunnerMixin {
  operator fun plus(builder: TCSettingsRunner.() -> Unit): TCRunnerMixinBuilder
  operator fun plus(builder: TCRunnerMixin): TCRunnerMixinBuilder = this + builder.asBuilder()
}

interface TCRunnerBuilder : TCSettingsRunnerRef {
  operator fun plus(mixin: TCRunnerMixin): TCRunnerBuilder = this + mixin.asBuilder()
  operator fun plus(builder: TCSettingsRunner.() -> Unit): TCRunnerBuilder
}

fun TCWithSettings.runner(id: String?, runnerType: String? = null, builder: TCSettingsRunner.() -> Unit = {}): TCRunnerBuilder {
  val runner = TCSettingsRunner()
  val result = object : TCRunnerBuilder, TCSettingsRunnerRef by runner {
    operator override fun plus(builder: TCSettingsRunner.() -> Unit): TCRunnerBuilder = having(this) { runner.builder() }
  } + {
    this.id = id
  } + {
    if (runnerType != null) this.runnerType = runnerType
  } + builder

  settings {
    runners = (runners ?: listOf()) + runner
  }

  return result
}

fun runnerMixin(runnerType: String? = null, builder: TCSettingsRunner.() -> Unit = {}): TCRunnerMixinBuilder {
  class TCRunnerBuilderImpl(val actions: List<TCSettingsRunner.() -> Unit> = listOf()) : TCRunnerMixinBuilder {
    override fun asBuilder(): TCSettingsRunner.() -> Unit = { actions.forEach { it() } }
    override fun plus(builder: TCSettingsRunner.() -> Unit) = TCRunnerBuilderImpl(actions + builder)
  }

  return TCRunnerBuilderImpl() + {
    if (runnerType != null) this.runnerType = runnerType
  } + builder
}

val coverageIDEA: TCRunnerMixin = runnerMixin {
  param("teamcity.coverage.idea.includePatterns", "*")
}

val coverageEMMA: TCRunnerMixin = runnerMixin {
  param("teamcity.coverage.emma.include.source", "true")
  param("teamcity.coverage.emma.instr.parameters", "-ix -*Test*")
}

val coverageJOCOCO: TCRunnerMixin = runnerMixin {
  param("teamcity.coverage.jacoco.patterns", "+:*")
}

val normalStep: TCRunnerMixin = runnerMixin {
  param("teamcity.step.mode", "default")
}

val alwaysStep: TCRunnerMixin = runnerMixin {
  param("teamcity.step.mode", "execute_always")
}
