package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.having
import org.jonnyzzz.teamcity.dsl.model.TCMetaRunner
import org.jonnyzzz.teamcity.dsl.model.TCMetaRunnerRef
import org.jonnyzzz.teamcity.dsl.model.TCProjectRef

interface TCMetaRunnerMixin {
  fun asBuilder(): TCMetaRunner.() -> Unit
}

interface TCMetaRunnerBuilderMixin : TCMetaRunnerMixin {
  operator fun plus(mixin : TCMetaRunnerMixin) : TCMetaRunnerBuilderMixin = this + mixin.asBuilder()
  operator fun plus(builder : TCMetaRunner.() -> Unit) : TCMetaRunnerBuilderMixin
}

interface TCMetaRunnerBuilder : TCMetaRunnerRef {
  operator fun plus(mixin : TCMetaRunnerMixin) : TCMetaRunnerBuilder = this + mixin.asBuilder()
  operator fun plus(builder : TCMetaRunner.() -> Unit) : TCMetaRunnerBuilder
}

fun metaRunnerMixin(builder : TCMetaRunner.() -> Unit = {}) : TCMetaRunnerBuilderMixin {
  class TCMetaRunnerBuilderMixinImpl(val actions : List<TCMetaRunner.() -> Unit> = listOf()) : TCMetaRunnerBuilderMixin {
    override fun asBuilder(): TCMetaRunner.() -> Unit = { actions.forEach { it() } }
    override fun plus(builder: TCMetaRunner.() -> Unit) = TCMetaRunnerBuilderMixinImpl(actions + builder)
  }
  return TCMetaRunnerBuilderMixinImpl() + builder
}

fun TCProjectRef.metaRunner(id : String, builder : TCMetaRunner.() -> Unit = {}) : TCMetaRunnerBuilder {
  val builders = LazyBuilders(builder)
  val result = object : TCMetaRunner(), TCDSLLazy by builders {
    init {
      this.id = id
    }
  }
  builders.instance = result
  if (this is TCProjectRefOnReady) this.onReady { this.metaRunners += result }

  return object: TCMetaRunnerBuilder, TCMetaRunnerRef by result {
    operator override fun plus(builder: TCMetaRunner.() -> Unit): TCMetaRunnerBuilder = having(this) { builders.add(builder) }
  }
}
