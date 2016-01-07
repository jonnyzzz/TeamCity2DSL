package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.api.internal.DSLRegistry
import org.jonnyzzz.teamcity.dsl.having
import org.jonnyzzz.teamcity.dsl.model.TCPluginSettings
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.model.TCProjectRef
import org.jonnyzzz.teamcity.dsl.setIfNull
import kotlin.collections.forEach
import kotlin.collections.listOf
import kotlin.collections.plus

fun TCProject.cleanup(builder : TCUnknownBuilder.() -> Unit) {
  cleanup = elementImpl("cleanup", builder)
}

interface TCProjectPluginsBuilder {
  fun element(name : String, builder: TCUnknownBuilder.() -> Unit)
}

fun TCProject.plugins(builder : TCProjectPluginsBuilder.() -> Unit) {
  val settings = setIfNull(TCProject::pluginSettings) { TCPluginSettings() }
  object: TCProjectPluginsBuilder {
    override fun element(name: String, builder: TCUnknownBuilder.() -> Unit) {
      settings.settings = (settings.settings ?: listOf()) + elementImpl(name, builder)
    }
  }.builder()
}

interface  TCProjectRefOnReady {
  fun onReady(builder: TCProject.() -> Unit)
}

class UnknownProject(val projectId : String) : TCProjectRef {
  override val id = projectId
}

object RootProject : TCProjectRef {
  override val id = null
}

interface  TCProjectMixin {
  fun asBuilder(): TCProject.() -> Unit
}

interface  TCProjectMixinBuilder : TCProjectMixin {
  operator fun plus(mixin : TCProjectMixin) : TCProjectMixinBuilder = this + mixin.asBuilder()
  operator fun plus(builder : TCProject.() -> Unit) : TCProjectMixinBuilder
}

interface  TCProjectBuilder : TCProjectRef {
  operator fun plus(mixin : TCProjectMixin) : TCProjectBuilder = this + mixin.asBuilder()
  operator fun plus(builder : TCProject.() -> Unit) : TCProjectBuilder
}

fun projectMixin(builder : TCProject.() -> Unit = {}) : TCProjectMixinBuilder {
  class TCProjectMixinBuilderImpl(val actions : List<TCProject.() -> Unit> = listOf()) : TCProjectMixinBuilder {
    override fun asBuilder(): TCProject.() -> Unit = { actions.forEach { it() } }
    override fun plus(builder: TCProject.() -> Unit) = TCProjectMixinBuilderImpl(actions + builder)
  }
  return TCProjectMixinBuilderImpl() + builder
}

fun TCProjectRef.project(id : String, builder : TCProject.() -> Unit = {}) : TCProjectBuilder {
  val parentId = this.id
  val builders = LazyBuilders(builder)
  val result = object : TCProject(), TCDSLLazy by builders {
    init {
      this.id = id
      if (parentId != null) this.parentId = parentId
    }
  }
  builders.instance = result

  DSLRegistry.addCompletedProject(p = result)

  return object: TCProjectBuilder, TCProjectRef by result, TCProjectRefOnReady {
    override fun plus(builder: TCProject.() -> Unit): TCProjectBuilder = having(this) { builders.add(builder) }

    override fun onReady(builder: TCProject.() -> Unit) {
      this + builder
    }
  }
}
