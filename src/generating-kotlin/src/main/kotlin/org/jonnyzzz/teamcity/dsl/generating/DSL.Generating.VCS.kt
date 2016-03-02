package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.api.gitRoot
import org.jonnyzzz.teamcity.dsl.api.vcsRootMixin
import org.jonnyzzz.teamcity.dsl.clustering.DSLClusteringGenerator
import org.jonnyzzz.teamcity.dsl.clustering.clusteringVCS
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.model.TCSettingsVCSRef
import org.jonnyzzz.teamcity.dsl.model.TCVCSRoot
import java.util.*
import kotlin.collections.forEach
import kotlin.collections.linkedMapOf
import kotlin.collections.map
import kotlin.collections.toTypedArray
import kotlin.text.*

val TCVCSRoot.variableName : String
  get() = "VCS_" + id!!

val TCSettingsVCSRef.variableName : String
  get() = "VCS_" + rootId!!


fun KotlinWriter.generateBuildSettingsVcsRoot(context : GenerationContext, it : TCSettingsVCSRef) {
  block(when(context.isDeclared(it)){
    true -> "vcs(${it.variableName})"
    else -> "vcs(UnknownVCSRoot(${it.rootId?.quote()}))"
  }) {
    it.checkoutRule?.forEach { rule ->
      rule
              .split("[\r\n]+".toRegex()).toTypedArray()
              .map { it.trim() }
              .forEach {
                ///TODO: split '+: a => b' into   '+ "a" to "b"' expr
                if (it.startsWith("+:")) {
                  appendln("+ ${it.substring(2).quote()}")
                } else if (it.startsWith("-:")) {
                  appendln("- ${it.substring(2).quote()}")
                } else {
                  appendln("rule(${it.quote()})")
                }
              }
    }
  }
}

fun KotlinWriter.generateVCSRoots(context : GenerationContext, project : TCProject, roots : List<TCVCSRoot>) {
  val that = this
  with(object : DSLClusteringGenerator<TCVCSRoot>() {
    override fun nameDMixin(d: TCVCSRoot): String = "${project.className}_vcsMixin"
    override fun funDMixin(d: TCVCSRoot): String = ::vcsRootMixin.name
    override fun funD(d: TCVCSRoot): String = "val ${d.variableName} = ${project.nameOrRef(context)}.vcsRoot(${d.id?.quote()})"
    override fun newD(): TCVCSRoot = TCVCSRoot()


    override fun predefinedMixins(): LinkedHashMap<String, TCVCSRoot.() -> Unit> = linkedMapOf(
            "gitRoot" to gitRoot.asBuilder()
    )

    override fun detectMixins(ds: List<TCVCSRoot>): List<TCVCSRoot.() -> Unit> = clusteringVCS(ds)

    override fun KotlinWriter.generateImplementationBlock(item: TCVCSRoot, baseItem: TCVCSRoot) {
      setter("vcsType", item.vcsType)

      setter("name", item.name)
      setter("modificationCheckInterval", item.modificationCheckInterval)

      appendln()
      params(item.parameters, baseItem.parameters)
    }

    override fun KotlinWriter.generatePostBlock(item: TCVCSRoot, baseItem: TCVCSRoot) {
      appendln()
    }

  }) {
    that.generate(roots).let { f -> roots.forEach { f(it) }}
  }
}
