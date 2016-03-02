package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.*


interface TCBuildSettingsDependencyBuilder {
  fun artifact(id : String? = null, builder : TCSettingsArtifactDependency.() -> Unit)
  fun snapshot(builder : TCSettingsSnapshotDependency.() -> Unit)
}

fun TCWithSettings.dependency(build : TCBuildTypeRef, builder : TCBuildSettingsDependencyBuilder.() -> Unit) {
  val buildTypeId = build.id!!
  object : TCBuildSettingsDependencyBuilder {
    override fun artifact(id : String?, builder: TCSettingsArtifactDependency.() -> Unit) {
      settings {
        artifactDependencies = (artifactDependencies ?: listOf()) + TCSettingsArtifactDependency().apply {
          this.id = id
          this.buildTypeId = buildTypeId
          this.builder()
        }
      }
    }
    override fun snapshot(builder: TCSettingsSnapshotDependency.() -> Unit) {
      settings {
        snapshotDependencies = (snapshotDependencies ?: listOf()) + TCSettingsSnapshotDependency().apply {
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

  opts.options = (opts.options ?: listOf()) + TCSettingsOption().apply {
    this.name = name
    this.value = value
  }
}
