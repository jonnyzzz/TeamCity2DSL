package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.*

fun TCBuildType.runner(id: String, runnerType : String? = null, builder: TCSettingsRunner.() -> Unit = {}): TCRunnerBuilder {
  return (this as TCWithSettings).runner(id, runnerType, builder).apply {
    runnerRef(id)
  }
}

fun TCBuildType.runnerRef(id : String) {
  with(settings) {
    runnersOrder = (runnersOrder ?: listOf()) + id
  }
}

class UnknownBuild(buildId : String) : TCBuildTypeRef {
  override val id: String? = buildId
}

interface  TCBuildTypeMixin {
  fun asBuilder(): TCBuildType.() -> Unit
}

interface  TCBuildTypeMixinBuilder : TCBuildTypeMixin {
  operator fun plus(mixin : TCBuildTemplateRef) : TCBuildTypeMixinBuilder = this + {settings.templateId = mixin.id }
  operator fun plus(mixin : TCBuildTypeMixin) : TCBuildTypeMixinBuilder = this + mixin.asBuilder()
  operator fun plus(builder : TCBuildType.() -> Unit) : TCBuildTypeMixinBuilder
}

interface  TCBuildTypeBuilder : TCBuildTypeRef {
  operator fun plus(mixin : TCBuildTemplateRef) : TCBuildTypeBuilder = this + {settings.templateId = mixin.id }
  operator fun plus(mixin : TCBuildTypeMixin) : TCBuildTypeBuilder = this + mixin.asBuilder()
  operator fun plusAssign(mixin : TCBuildTypeMixin) : Unit { this + mixin }
  operator fun plus(builder : TCBuildType.() -> Unit) : TCBuildTypeBuilder
}

fun buildMixin(builder : TCBuildType.() -> Unit = {}) : TCBuildTypeMixinBuilder {
  class TCBuildTypeMixinBuilderImpl(val actions : List<TCBuildType.() -> Unit> = listOf()) : TCBuildTypeMixinBuilder {
    override fun asBuilder(): TCBuildType.() -> Unit = { actions.forEach { it() } }
    override fun plus(builder: TCBuildType.() -> Unit) = TCBuildTypeMixinBuilderImpl(actions + builder)
  }
  return TCBuildTypeMixinBuilderImpl() + builder
}

fun TCProjectRef.build(id : String, builder : TCBuildType.() -> Unit = {}) : TCBuildTypeBuilder {
  val builders = LazyBuilders(builder)
  val result = object : TCBuildType(id), TCDSLLazy by builders { }
  builders.instance = result
  if (this is TCProjectRefOnReady) this.onReady { this.buildTypes += result }

  return object: TCBuildTypeBuilder, TCBuildTypeRef by result  {
    operator override fun plus(builder: TCBuildType.() -> Unit): TCBuildTypeBuilder = this.apply { builders.add(builder) }
  }
}
