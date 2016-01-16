package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.having
import org.jonnyzzz.teamcity.dsl.model.TCSettingsExtension
import org.jonnyzzz.teamcity.dsl.model.TCSettingsExtensionRef
import org.jonnyzzz.teamcity.dsl.model.TCWithSettings


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

fun TCWithSettings.extension(id : String, extensionType : String, builder : TCSettingsExtension.() -> Unit = {}) : TCSettingsExtensionBuilder {
  val extension = having(TCSettingsExtension()) {
    this.id = id
    this.extensionType = extensionType
  }

  settings {
    extensions = (extensions ?: listOf()) + extension
  }

  return object : TCSettingsExtensionBuilder, TCSettingsExtensionRef by extension {
    override fun plus(builder: TCSettingsExtension.() -> Unit) = having(this) { extension.builder() }
  } + builder
}
