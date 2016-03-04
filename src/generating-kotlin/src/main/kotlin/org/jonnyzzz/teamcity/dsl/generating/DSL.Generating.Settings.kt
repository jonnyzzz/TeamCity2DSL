package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.model.TCBuildSettings
import kotlin.collections.forEach


fun KotlinWriter.generateSettings(context : GenerationContext, settings : TCBuildSettings?, generateSettingsRunners : KotlinWriter.() -> Unit) {
  if (settings == null) return

  generateDependencies(context, settings)

  generateSettingsOptions(settings.options)

  settings.disabledSettings?.forEach {
    appendln("disable(${it.quote()})")
  }

  generateSettingsRunners()

  settings.extensions?.forEach {
    block("extension(${it.id?.quote()}, ${it.extensionType?.quote()})") {
      params(it.parameters)
    }
  }

  settings.vcs?.forEach { generateBuildSettingsVcsRoot(context, it) }

  generateRequirements(settings.requirements)

  val cleanup = settings.cleanup
  if (cleanup != null) {
    block("cleanup") {
      elementInternals(cleanup)
    }
  }

  val buildTriggers = settings.buildTriggers
  if (buildTriggers != null) {
    block("triggers") {
      buildTriggers.forEach {
        block("trigger(${it.id?.quote()}, ${it.triggerType?.quote()})") {
          it.parameters?.forEach {
            appendln("param(${it.name?.quote()}, ${it.value?.quote()})")
          }
        }
      }
    }
  }
}
