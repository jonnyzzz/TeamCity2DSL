package org.jonnyzzz.teamcity.dsl.clustering

import org.jonnyzzz.kotlin.xml.bind.jdom.JDOM
import org.jonnyzzz.teamcity.dsl.api.param
import org.jonnyzzz.teamcity.dsl.api.vcsRootMixin
import org.jonnyzzz.teamcity.dsl.model.TCParameter
import org.jonnyzzz.teamcity.dsl.model.TCVCSRoot
import kotlin.collections.*

fun clusteringVCS(runners : List<TCVCSRoot>) : List<TCVCSRoot.() -> Unit> {
  return runners
          //consider only groups with same runners
          .groupBy { it.vcsType }
          //extract runner only for groups with >= 2 elements
          .filter { it.value.size >= 2 }
          //cluster by parameters
          .flatMap { clusteringVCSByParameters(it.value) }
}


fun clusteringVCSByParameters(runners : List<TCVCSRoot>) : List<TCVCSRoot.() -> Unit>{
  assert(runners.map { it.vcsType }.toSet().size == 1) { "Must be only one vcsType VCS Roots" }
  if (runners.size < 2) return listOf()

  class P(val param : TCParameter) {
    override fun equals(other: Any?): Boolean = other is P && other.param.name == this.param.name && other.param.value == this.param.value
    override fun hashCode(): Int = param.name?.hashCode() ?: 42
    override fun toString(): String = "P(${param.name}->${param.value})"
  }

  class R(val param : TCVCSRoot) {
    override fun equals(other: Any?): Boolean =  other is R && other.param.id == this.param.id
    override fun hashCode(): Int = param.id.hashCode()
    override fun toString(): String = "R(${param.id})"
  }

  return object : DSLClustering<TCVCSRoot, TCVCSRoot.() -> Unit, P, R>() {
    override fun extractPs(r: TCVCSRoot): List<P> = r.parameters?.map { P(it) } ?: listOf()
    override fun extractRs(r: TCVCSRoot): R = R(r)

    override fun builderPs(ps: List<P>): TCVCSRoot.() -> Unit = ps
            .map { JDOM.clone(it.param) }
            .toList()
            .let { paramz ->
              vcsRootMixin {
                paramz.forEach { p -> param(p.name!!, p.value!!) }
              }.asBuilder()
            }
  }.cluster(runners)
}

