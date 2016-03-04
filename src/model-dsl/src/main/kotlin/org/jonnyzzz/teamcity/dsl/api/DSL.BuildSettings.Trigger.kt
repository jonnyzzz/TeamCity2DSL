package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.TCSettingsTrigger
import org.jonnyzzz.teamcity.dsl.model.TCSettingsTriggerRef
import org.jonnyzzz.teamcity.dsl.model.TCWithSettings

interface TCSettingsTriggerMixin {
  fun asBuilder(): TCSettingsTrigger.() -> Unit
}

interface TCSettingsTriggerMixinBuilder : TCSettingsTriggerMixin {
  operator fun plus(mixin: TCSettingsTriggerMixin): TCSettingsTriggerMixinBuilder = this + mixin.asBuilder()
  operator fun plus(builder: TCSettingsTrigger.() -> Unit): TCSettingsTriggerMixinBuilder
}

interface TCSettingsTriggerBuilder : TCSettingsTriggerRef {
  operator fun plus(mixin: TCSettingsTriggerMixin): TCSettingsTriggerBuilder = this + mixin.asBuilder()
  operator fun plus(builder: TCSettingsTrigger.() -> Unit): TCSettingsTriggerBuilder
}

fun triggerMixin(builder: TCSettingsTrigger.() -> Unit = {}): TCSettingsTriggerMixinBuilder {
  class TCSettingsTriggerMixinBuilderImpl(val actions: List<TCSettingsTrigger.() -> Unit> = listOf()) : TCSettingsTriggerMixinBuilder {
    override fun asBuilder(): TCSettingsTrigger.() -> Unit = { actions.forEach { it() } }
    override fun plus(builder: TCSettingsTrigger.() -> Unit) = TCSettingsTriggerMixinBuilderImpl(actions + builder)
  }
  return TCSettingsTriggerMixinBuilderImpl() + builder
}

interface TriggersBuilder {
  fun trigger(triggerId: String, triggerType: String, builder: TCSettingsTrigger.() -> Unit = {}): TCSettingsTriggerBuilder
}

fun TCWithSettings.triggers(builder : TriggersBuilder.() -> Unit) {
  settings {
    buildTriggers = (buildTriggers ?: listOf())
  }

  object:TriggersBuilder {
    override fun trigger(triggerId: String, triggerType: String, builder: TCSettingsTrigger.() -> Unit) : TCSettingsTriggerBuilder{
      val trigger = TCSettingsTrigger().apply {
        this.id = triggerId
        this.triggerType = triggerType
      }

      settings {
        buildTriggers = (buildTriggers ?: listOf()) + trigger
      }

      return object : TCSettingsTriggerBuilder, TCSettingsTriggerRef by trigger {
        operator override fun plus(builder: TCSettingsTrigger.() -> Unit): TCSettingsTriggerBuilder = this.apply { trigger.builder() }

      } + builder
    }
  }.builder()
}
