package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.TCBuildTemplate
import org.jonnyzzz.teamcity.dsl.model.TCBuildTemplateRef
import org.jonnyzzz.teamcity.dsl.model.TCProjectRef

class UnknownTemplate(val templateId : String) : TCBuildTemplateRef {
  override val id = templateId
}

interface  TCTemplateMixin {
  fun asBuilder(): TCBuildTemplate.() -> Unit
}

interface  TCTemplateMixinBuilder : TCTemplateMixin {
  operator fun plus(mixin : TCTemplateMixin) : TCTemplateMixinBuilder = this + mixin.asBuilder()
  operator fun plus(builder : TCBuildTemplate.() -> Unit) : TCTemplateMixinBuilder
}

interface  TCTemplateBuilder : TCBuildTemplateRef {
  operator fun plus(mixin : TCTemplateMixin) : TCTemplateBuilder = this + mixin.asBuilder()
  operator fun plusAssign(mixin : TCTemplateMixin) : Unit { this + mixin }
  operator fun plus(builder : TCBuildTemplate.() -> Unit) : TCTemplateBuilder
}

fun templateMixin(builder : TCBuildTemplate.() -> Unit = {}) : TCTemplateMixinBuilder {
  class TCTemplateMixinBuilderImpl(val actions : List<TCBuildTemplate.() -> Unit> = listOf()) : TCTemplateMixinBuilder {
    override fun asBuilder(): TCBuildTemplate.() -> Unit = { actions.forEach { it() } }
    override fun plus(builder: TCBuildTemplate.() -> Unit) = TCTemplateMixinBuilderImpl(actions + builder)
  }
  return TCTemplateMixinBuilderImpl() + builder
}

fun TCProjectRef.template(id : String, builder : TCBuildTemplate.() -> Unit = {}) : TCTemplateBuilder {
  val builders = LazyBuilders(builder)
  val result = object : TCBuildTemplate(), TCDSLLazy by builders {
    init {
      this.id = id
    }
  }
  builders.instance = result
  if (this is TCProjectRefOnReady) this.onReady { this.buildTemplates += result }

  return object: TCTemplateBuilder, TCBuildTemplateRef by result  {
    operator override fun plus(builder: TCBuildTemplate.() -> Unit): TCTemplateBuilder = this.apply { builders.add(builder) }
  }
}
