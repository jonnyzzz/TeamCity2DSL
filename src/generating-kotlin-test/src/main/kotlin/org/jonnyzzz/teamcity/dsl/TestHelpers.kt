package org.jonnyzzz.teamcity.dsl

import org.jonnyzzz.teamcity.dsl.generating.DSLOptions
import org.jonnyzzz.teamcity.dsl.generating.GenerationContext
import org.jonnyzzz.teamcity.dsl.model.*


val context = object : GenerationContext {
  override val projects: List<TCProject>
    get() = throw UnsupportedOperationException()
  override val version: TeamCityVersion
    get() = throw UnsupportedOperationException()

  override val options: DSLOptions = DSLOptions()

  override fun isDeclared(project: TCProject): Boolean = false
  override fun isDeclared(root: TCVCSRoot): Boolean = false
  override fun isDeclared(ref: TCSettingsVCSRef): Boolean = false
  override fun findBuild(buildId: String?): TCBuildType? = null
  override fun findTemplate(templateId: String?): TCBuildTemplate? = null
  override fun findProject(projectId: String?): TCProject? = null
}

val contextWithLookup = object: GenerationContext by context {
  override fun findBuild(buildId: String?): TCBuildType? = buildId?.let { object: TCBuildType(buildId) { } }
  override fun findProject(projectId: String?): TCProject? = projectId?.let { object: TCProject(projectId) { } }
}

