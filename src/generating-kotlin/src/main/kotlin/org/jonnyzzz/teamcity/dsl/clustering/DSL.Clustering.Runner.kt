package org.jonnyzzz.teamcity.dsl.clustering

import org.jonnyzzz.kotlin.xml.bind.jdom.JDOM
import org.jonnyzzz.teamcity.dsl.api.param
import org.jonnyzzz.teamcity.dsl.api.runnerMixin
import org.jonnyzzz.teamcity.dsl.model.TCParameter
import org.jonnyzzz.teamcity.dsl.model.TCSettingsRunner
import kotlin.collections.*


fun clusteringRunner(runners : List<TCSettingsRunner>) : List<TCSettingsRunner.() -> Unit> {
  return runners
          //consider only groups with same runners
          .groupBy { it.runnerType }
          //extract runner only for groups with >= 2 elements
          .filter { it.value.size >= 2 }
          //cluster by parameters
          .flatMap { clusteringRunnersByParameters(it.value) }
}

fun clusteringRunnersByParameters(runners : List<TCSettingsRunner>) : List<TCSettingsRunner.() -> Unit>{
  assert(runners.map { it.runnerType }.toSet().size == 1) { "Must be only one runnerType runners" }
  if (runners.size < 2) return listOf()

  class P(val param : TCParameter) {
    override fun equals(other: Any?): Boolean = other is P && other.param.name == this.param.name && other.param.value == this.param.value
    override fun hashCode(): Int = param.name?.hashCode() ?: 42
    override fun toString(): String = "P(${param.name}->${param.value})"
  }

  class R(val param : TCSettingsRunner) {
    override fun equals(other: Any?): Boolean =  other is R && other.param.id == this.param.id
    override fun hashCode(): Int = param.id?.hashCode() ?: 42
    override fun toString(): String = "R(${param.id})"
  }

  return object : DSLClustering<TCSettingsRunner, TCSettingsRunner.() -> Unit, P, R>() {
    override fun extractPs(r: TCSettingsRunner): List<P> = r.parameters?.map { P(it) } ?: listOf()
    override fun extractRs(r: TCSettingsRunner): R = R(r)

    override fun builderPs(ps: List<P>): TCSettingsRunner.() -> Unit = ps
            .map { JDOM.clone(it.param) }
            .toList()
            .let { paramz ->
              runnerMixin {
                paramz.forEach { p -> param(p.name!!, p.value!!) }
              }.asBuilder()
            }
  }.cluster(runners)
}
