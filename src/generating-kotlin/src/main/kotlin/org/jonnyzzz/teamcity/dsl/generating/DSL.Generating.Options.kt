package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.api.*
import org.jonnyzzz.teamcity.dsl.model.TCSettingsOption
import org.jonnyzzz.teamcity.dsl.model.TCSettingsOptions
import kotlin.collections.*
import kotlin.sequences.firstOrNull
import kotlin.sequences.map
import kotlin.text.split
import kotlin.text.toLong
import kotlin.text.toRegex


fun KotlinWriter.generateSettingsOptions(options : TCSettingsOptions?) {
  val customRenders = listOf(
          ::generateSettingsExecutionTimeout,
          ::generateArtifactsRefs,
          ::generateSettingsBuildNumberPattern,
          ::generateSettingsMaximumParallelBuilds,
          ::generateSettingsCheckoutMode,
          ::generateSettingsCleanSources
  ).asSequence()

  val items = options?.options
  if (items?.any() ?: false) {
    block("options") {
      abstractParams(items, "option", customRender = { option -> customRenders.map { it(option) }.firstOrNull {it != null} })
    }
  }
}

fun generateSettingsCheckoutMode(opt: TCSettingsOption): (KotlinWriter.() -> Unit)? {
  if (opt.name != BuildOptions_CheckoutMode) return null
  if (opt.value != BuildOptions_CheckoutMode_ON_AGENT) return null
  return {
    appendln("agentSideCheckout()")
  }
}

fun generateSettingsCleanSources(opt: TCSettingsOption): (KotlinWriter.() -> Unit)? {
  if (opt.name != BuildOptions_CleanBuild) return null
  if (opt.value != "true") return null
  return {
    appendln("cleanSources()")
  }
}

fun generateSettingsExecutionTimeout(opt: TCSettingsOption): (KotlinWriter.() -> Unit)? {
  if (opt.name != BuildOptions_ExecutionTimeout) return null

  try {
    val timeout = opt.value!!.toLong()
    return {
      appendln("executionTimeout = $timeout")
    }
  } catch (t : Throwable) {
    return null
  }
}

fun generateSettingsMaximumParallelBuilds(opt: TCSettingsOption): (KotlinWriter.() -> Unit)? {
  if (opt.name != BuildOptions_MaximumNumberOfBuilds) return null

  try {
    val builds = opt.value!!.toLong()
    return {
      appendln("maximumParallelBuilds = $builds")
    }
  } catch (t : Throwable) {
    return null
  }
}

fun generateSettingsBuildNumberPattern(opt: TCSettingsOption): (KotlinWriter.() -> Unit)? {
  if (opt.name != BuildOptions_BuildNumberPattern) return null

  val v = opt.value ?: return null

  return {
    appendln("buildNumberPattern = ${v.quoteWithRefs()}")
  }
}

fun generateArtifactsRefs(opt: TCSettingsOption): (KotlinWriter.() -> Unit)? {
  if (opt.name != BuildOptions_ArtifactRules) return null

  val refs = opt.value?.splitNewLines() ?: return null

  return {
    block("publishArtifacts") {
      refs.forEach { ref ->
        val seps = ref.split(" => ".toRegex()).toTypedArray()
        if (seps.size == 2) {
          appendln("+ ${seps[0].quoteWithRefs()} to ${seps[1].quoteWithRefs()}")
        } else {
          appendln("+ ${ref.quoteWithRefs()}")
        }
      }
    }
  }
}

