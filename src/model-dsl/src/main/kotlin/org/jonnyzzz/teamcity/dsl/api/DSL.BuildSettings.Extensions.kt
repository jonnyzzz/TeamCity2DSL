package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.TCBuildSettings
import org.jonnyzzz.teamcity.dsl.model.TCSettingsExtension
import org.jonnyzzz.teamcity.dsl.model.TCSettingsExtensionRef

interface TCSettingsExtensionMixin {
  fun asBuilder(): TCSettingsExtension.() -> Unit
}

interface TCSettingsExtensionMixinBuilder : TCSettingsExtensionMixin {
  operator fun plus(mixin: TCSettingsExtensionMixin): TCSettingsExtensionMixinBuilder = this + mixin.asBuilder()
  operator fun plus(builder: TCSettingsExtension.() -> Unit): TCSettingsExtensionMixinBuilder
}

interface TCSettingsExtensionBuilder : TCSettingsExtensionRef {
  operator fun plus(mixin: TCSettingsExtensionMixin): TCSettingsExtensionBuilder = this + mixin.asBuilder()
  operator fun plus(builder: TCSettingsExtension.() -> Unit): TCSettingsExtensionBuilder
}

fun extensionMixin(extensionType : String? = null, builder: TCSettingsExtension.() -> Unit = {}): TCSettingsExtensionMixinBuilder {
  class TCSettingsExtensionMixinBuilderImpl(val actions: List<TCSettingsExtension.() -> Unit> = listOf()) : TCSettingsExtensionMixinBuilder {
    override fun asBuilder(): TCSettingsExtension.() -> Unit = { actions.forEach { it() } }
    override fun plus(builder: TCSettingsExtension.() -> Unit) = TCSettingsExtensionMixinBuilderImpl(actions + builder)
  }
  return TCSettingsExtensionMixinBuilderImpl() + {
    if (extensionType != null) this.extensionType = extensionType
  } + builder
}

fun TCBuildSettings.extension(id : String, extensionType : String, builder : TCSettingsExtension.() -> Unit = {}) : TCSettingsExtensionBuilder {
  val extension = TCSettingsExtension().apply {
    this.id = id
    this.extensionType = extensionType
  }

  extensions = (extensions ?: listOf()) + extension

  return object : TCSettingsExtensionBuilder, TCSettingsExtensionRef by extension {
    override fun plus(builder: TCSettingsExtension.() -> Unit) = this.apply { extension.builder() }
  } + builder
}
