package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.api.internal.DSLRegistry
import org.jonnyzzz.teamcity.dsl.model.*
import org.jonnyzzz.teamcity.dsl.setIfNull
import kotlin.comparisons.compareBy

interface TCProjectOrderingBuilder {
  operator fun TCProjectRef.unaryPlus()
  operator fun TCBuildTypeRef.unaryPlus()
}

fun TCProject.ordering(builder : TCProjectOrderingBuilder.() -> Unit) {
  val o = ordering ?: TCProjectOrdering()
  ordering = o

  object : TCProjectOrderingBuilder {
    override fun TCProjectRef.unaryPlus() {
      o.projectsOrder = (o.projectsOrder ?: listOf()) + this.id!!
    }

    override fun TCBuildTypeRef.unaryPlus() {
      o.buildsOrder = (o.buildsOrder ?: listOf()) + this.id!!
    }
  }.builder()
}

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
  operator fun plusAssign(mixin : TCProjectMixin) : Unit { this + mixin }
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
  val result = object : TCProject(id), TCDSLLazy by builders {
    init {
      if (parentId != null) this.parentId = parentId
    }
  }
  builders.instance = result

  DSLRegistry.addCompletedProject(result)

  return object: TCProjectBuilder, TCProjectRef by result, TCProjectRefOnReady {
    override fun plus(builder: TCProject.() -> Unit): TCProjectBuilder = this.apply { builders.add(builder) }

    override fun onReady(builder: TCProject.() -> Unit) {
      this + builder
    }
  }
}

interface TCProjectExtensionBuilder {
  fun param(name : String, value : String? = null, builder: TCParameterBuilder.() -> Unit = {})

}

interface TCProjectExtensionsBuilder {
  fun extension(id : String, type : String, builder : TCProjectExtensionBuilder.() -> Unit)
}

fun TCProject.extensions(builder : TCProjectExtensionsBuilder.() -> Unit) {
  val host = TCProjectExtensions()
  projectExtensions = host

  object : TCProjectExtensionsBuilder {
    override fun extension(id: String, type: String, builder: TCProjectExtensionBuilder.() -> Unit) {
      val ext = TCProjectExtension()
      ext.id = id
      ext.type = type

      object : TCProjectExtensionBuilder {
        override fun param(name: String, value: String?, builder: TCParameterBuilder.() -> Unit) {
          ext.addParameter(TCProjectExtension::parameters, name, value, builder)
          ext.parameters = ext.parameters?.sortedWith(compareBy { it.name ?: "" })
        }
      }.builder()

      host.extensions = (host.extensions ?: listOf()) + ext
    }
  }.builder()
}
