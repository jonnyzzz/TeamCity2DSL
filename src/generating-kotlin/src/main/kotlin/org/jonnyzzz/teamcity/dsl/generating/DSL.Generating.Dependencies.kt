package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.model.TCBuildSettings
import org.jonnyzzz.teamcity.dsl.model.TCSettingsArtifactDependency
import org.jonnyzzz.teamcity.dsl.model.TCSettingsSnapshotDependency


fun KotlinWriter.generateDependencies(context : GenerationContext, settings : TCBuildSettings) {
  data class BuildTypeId(val id : String) : Comparable<BuildTypeId> {
    override fun compareTo(other: BuildTypeId): Int = id.compareTo(other.id, ignoreCase = true)
  }

  data class Dependency(val artifacts: List<TCSettingsArtifactDependency> = listOf(), val snapshot: TCSettingsSnapshotDependency? = null)

  operator fun Dependency?.plus(a : TCSettingsArtifactDependency) : Dependency {
    return Dependency((this?.artifacts ?: listOf()) + a, this?.snapshot)
  }

  operator fun Dependency?.plus(s : TCSettingsSnapshotDependency) : Dependency {
    assert(this?.snapshot == null)
    return Dependency(this?.artifacts ?: listOf(), s)
  }

  fun renderDependency(id : BuildTypeId, dep : Dependency) {
    fun dependencyRef() : String {
      val buildId = id.id
      val build = context.findBuild(buildId)
      if (build != null) return build.variableName
      return "UnknownBuild(${buildId.quote()})"
    }

    block("dependency(${dependencyRef()})") {
      dep.artifacts.forEach { artifact ->
        block("artifact") {
          setter("cleanDestination", artifact.cleanDestination)
          setter("cleanDestinationAfterBuild", artifact.cleanDestinationAfterBuild)
          setter("revision", artifact.revision)
          setter("revisionRuleName", artifact.revisionRuleName)
          setter("branch", artifact.branch)

          block("pattern") {
            artifact.artifactPattern?.splitNewLines("\r\n")?.forEach {
              //TODO split +: a => b into expr of a and b
              when {
                it.startsWith("+:") -> appendln("+ ${it.substring(2).quoteWithRefs()}")
                it.startsWith("-:") -> appendln("- ${it.substring(2).quoteWithRefs()}")
                else -> appendln("rule(${it.quoteWithRefs()})")
              }
            }
          }
        }
      }

      val snapshot = dep.snapshot
      if (snapshot != null) {
        block("snapshot") {
          snapshot.options?.options?.forEach {
            appendln("param(${it.name?.quote()}, ${it.value?.quote()})")
          }
        }
      }

    }
  }

  val deps = (settings.artifactDependencies ?: listOf()).map { BuildTypeId(it.buildTypeId!!) to Dependency() + it }.toMutableList()
  var min = 0
  settings.snapshotDependencies?.forEach {
    val bt = BuildTypeId(it.buildTypeId!!)

    var found = false
    for (i in min..(deps.size - 1)) {
      if (bt == deps[i].first) {
        deps[i] = bt to deps[i].second + it
        min = i + 1
        found = true
        break
      }
    }

    if (!found) {
      deps.add(min++, bt to Dependency() + it)
    }
  }

  deps.forEach {
    renderDependency(it.first, it.second)
  }

}
