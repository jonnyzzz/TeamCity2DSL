package org.jonnyzzz.teamcity.dsl.extensions

import org.jonnyzzz.teamcity.dsl.lang.api.BuildRunnerExtensionGenerator
import org.jonnyzzz.teamcity.dsl.lang.api.BuildRunnerGenerator
import org.jonnyzzz.teamcity.dsl.lang.api.ExtensionPriority
import java.util.*

object Host {
  val runnerExtensions by lazy { load(BuildRunnerExtensionGenerator::class.java) }
  val runners by lazy { load(BuildRunnerGenerator::class.java) }


  private fun <T : ExtensionPriority> load(clazz : Class<T>) : List<T> {
    val result = ServiceLoader.load(clazz)
            .toList()
            .sortedBy { it.priority }
            .toList()

    println(buildString {
      appendln("Loaded extensions of type ${clazz.simpleName}:")
      result.map { it.javaClass.simpleName }.sorted().forEach {
        appendln("  $it")
      }
      appendln()
    })

    return result
  }
}
