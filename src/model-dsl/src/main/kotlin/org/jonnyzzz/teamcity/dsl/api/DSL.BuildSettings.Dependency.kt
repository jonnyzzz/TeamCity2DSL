package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.having
import org.jonnyzzz.teamcity.dsl.model.*


interface TCBuildSettingsDependencyBuilder {
  fun artifact(builder : TCSettingsArtifactDependency.() -> Unit)
  fun snapshot(builder : TCSettingsSnapshotDependency.() -> Unit)
}

fun TCWithSettings.dependency(build : TCBuildTypeRef, builder : TCBuildSettingsDependencyBuilder.() -> Unit) {
  val buildTypeId = build.id!!
  object : TCBuildSettingsDependencyBuilder {
    override fun artifact(builder: TCSettingsArtifactDependency.() -> Unit) {
      settings {
        artifactDependencies = (artifactDependencies ?: listOf()) + having(TCSettingsArtifactDependency()) {
          this.buildTypeId = buildTypeId
          this.builder()
        }
      }
    }
    override fun snapshot(builder: TCSettingsSnapshotDependency.() -> Unit) {
      settings {
        snapshotDependencies = (snapshotDependencies ?: listOf()) + having(TCSettingsSnapshotDependency()) {
          this.buildTypeId = buildTypeId
          this.builder()
        }
      }
    }
  }.builder()
}

interface TCSettingsArtifactDependencyPattern {
  operator fun String.unaryPlus() = rule("+:" + this)
  operator fun String.unaryMinus() = rule("-:" + this)
  fun rule(rule : String)
}

fun TCSettingsArtifactDependency.pattern(builder : TCSettingsArtifactDependencyPattern.() -> Unit) {
  val pt = arrayListOf<String>()
  object:TCSettingsArtifactDependencyPattern {
    override fun rule(rule: String) {
      pt.add(rule)
    }
  }.builder()

  artifactPattern = if (pt.any()) pt.joinToString("\r\n") else null
}


fun TCSettingsSnapshotDependency.param(name : String, value : String) {
  val opts = options ?: TCSettingsOptions()
  options = opts

  opts.options = (opts.options ?: listOf()) + having(TCSettingsOption()) {
    this.name = name
    this.value = value
  }
}
