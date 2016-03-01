package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.model.*
import org.jonnyzzz.teamcity.dsl.writeUTF
import java.io.File
import kotlin.collections.any
import kotlin.collections.firstOrNull
import kotlin.collections.flatMap
import kotlin.collections.forEach

class DSLOptions {
  var packageName : String = "org.jonnyzzz.teamcity.autodsl"
}

interface GenerationContext {
  val options : DSLOptions

  fun isDeclared(project : TCProject) : Boolean
  fun isDeclared(root : TCVCSRoot) : Boolean
  fun isDeclared(ref : TCSettingsVCSRef) : Boolean

  fun findBuild(buildId : String?) : TCBuildType?
  fun findTemplate(templateId : String?) : TCBuildTemplate?
  fun findProject(projectId : String?) : TCProject?
}


object DSLGenerating {
  fun generate(projects: List<TCProject>, file: File, options: DSLOptions = DSLOptions()) {
    file.mkdirs()

    if (!file.isDirectory) throw Error("Failed to cleanup destination folder")

    val context = object : GenerationContext {
      override val options: DSLOptions = options
      override fun isDeclared(project: TCProject): Boolean = projects.any {it.id == project.id }
      override fun isDeclared(root: TCVCSRoot): Boolean = projects.any {it.vcsRoots.any { it.id == root.id }}
      override fun isDeclared(ref : TCSettingsVCSRef): Boolean = projects.any {it.vcsRoots.any { it.id == ref.rootId }}

      override fun findBuild(buildId: String?): TCBuildType? = projects.flatMap { it.buildTypes }.firstOrNull { it.id == buildId }
      override fun findTemplate(templateId: String?): TCBuildTemplate? = projects.flatMap { it.buildTemplates }.firstOrNull { it.id == templateId }
      override fun findProject(projectId: String?): TCProject? = projects.firstOrNull { it.id == projectId}
    }

    for (project in projects) {
      generateProject(context, file, project, options)
    }

    generateUUIDsMap(context, file, projects, options)
  }

  private fun generateProject(context : GenerationContext, root : File, project : TCProject, options : DSLOptions) {
    val projectId = project.id ?: throw Error("Project should have an id")

    val home = root / projectId
    home.mkdirs()

    if (project.vcsRoots.any()) {
      (home / "vcs_roots.tcdsl.kt").writeUTF {
        generateKotlinDSL(options.packageName, "project_${projectId}_vcs_roots") {
          generateVCSRoots(context, project, project.vcsRoots)
        }
      }
    }

    generateProject(context, home, project)
    project.buildTypes.forEach { generateBuildType(context, home, project, it ) }
    project.buildTemplates.forEach { generateTemplate(context, home, project, it) }
    project.metaRunners.forEach { generateMetaRunner(context, home, project, it) }
  }
}



