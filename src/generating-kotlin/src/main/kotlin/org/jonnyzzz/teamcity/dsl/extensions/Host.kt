package org.jonnyzzz.teamcity.dsl.extensions

import org.jonnyzzz.teamcity.dsl.lang.api.BuildRunnerExtensionGenerator
import org.jonnyzzz.teamcity.dsl.lang.api.BuildRunnerGenerator
import org.jonnyzzz.teamcity.dsl.lang.api.ExtensionPriority
import java.util.*

object Host {
  val runnerExtensions = load(BuildRunnerExtensionGenerator::class.java)
  val runners = load(BuildRunnerGenerator::class.java)


  private fun <T : ExtensionPriority> load(clazz : Class<T>) =
    ServiceLoader.load(clazz, javaClass.classLoader)
            .toList()
            .sortedBy { it.priority }
            .toList()
}
