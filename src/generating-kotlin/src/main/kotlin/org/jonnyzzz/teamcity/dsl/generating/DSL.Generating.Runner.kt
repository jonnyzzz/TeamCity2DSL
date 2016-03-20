package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.api.*
import org.jonnyzzz.teamcity.dsl.clustering.DSLClusteringGenerator
import org.jonnyzzz.teamcity.dsl.clustering.clusteringRunner
import org.jonnyzzz.teamcity.dsl.model.TCBuildSettings
import org.jonnyzzz.teamcity.dsl.model.TCSettingsRunner
import java.util.*
import kotlin.collections.linkedMapOf

fun KotlinWriter.generateRunners(runners: List<TCSettingsRunner>?): KotlinWriter.(TCSettingsRunner) -> Unit {
  val that = this
  with(object : DSLClusteringGenerator<TCSettingsRunner>() {
    override fun nameDMixin(d: TCSettingsRunner): String = ::runnerMixin.name
    override fun funDMixin(d: TCSettingsRunner): String = ::runnerMixin.name + "()"
    override fun funD(d: TCSettingsRunner): String = "${TCBuildSettings::runner.name}(${d.id?.quote()}, ${d.runnerType?.quote()})"

    override fun predefinedMixins(): LinkedHashMap<String, TCSettingsRunner.() -> Unit> = linkedMapOf(
            ::normalStep.name to normalStep.asBuilder(),
            ::alwaysStep.name to alwaysStep.asBuilder(),
            ::coverageEMMA.name to coverageEMMA.asBuilder(),
            ::coverageIDEA.name to coverageIDEA.asBuilder(),
            ::coverageJOCOCO.name to coverageJOCOCO.asBuilder()
    )

    override fun detectMixins(ds: List<TCSettingsRunner>): List<TCSettingsRunner.() -> Unit> = clusteringRunner(ds)
    override fun newD(): TCSettingsRunner = TCSettingsRunner()

    override fun KotlinWriter.generateImplementationBlock(item: TCSettingsRunner, baseItem: TCSettingsRunner) {
      if (item.name != null) setter("name", item.name)

      params(item.parameters, baseItem.parameters)
    }
  }) {
    return that.generate(runners)
  }
}
