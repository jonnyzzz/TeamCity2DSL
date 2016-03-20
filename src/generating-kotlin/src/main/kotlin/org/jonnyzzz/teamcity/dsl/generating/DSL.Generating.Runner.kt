package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.api.*
import org.jonnyzzz.teamcity.dsl.clustering.DSLClusteringGenerator
import org.jonnyzzz.teamcity.dsl.clustering.clusteringRunner
import org.jonnyzzz.teamcity.dsl.model.TCBuildSettings
import org.jonnyzzz.teamcity.dsl.model.TCSettingsRunner
import java.util.*
import kotlin.collections.linkedMapOf

fun KotlinWriter.generateRunners(runners: List<TCSettingsRunner>?): KotlinWriter.(TCSettingsRunner) -> Unit {

  return { d ->
    val item = d

    block("${TCBuildSettings::runner.name}(${d.id?.quote()}, ${d.runnerType?.quote()})") {
      if (item.name != null) setter("name", item.name)

      params(item.parameters)
    }
  }
}
