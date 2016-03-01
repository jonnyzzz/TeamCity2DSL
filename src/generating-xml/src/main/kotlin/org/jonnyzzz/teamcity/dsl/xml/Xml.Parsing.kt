package org.jonnyzzz.teamcity.dsl.xml

import org.jdom2.Document
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaders
import org.jonnyzzz.kotlin.xml.bind.XRoot
import org.jonnyzzz.kotlin.xml.bind.jdom.JDOM
import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.getAnnotationRec
import org.jonnyzzz.teamcity.dsl.model.*
import java.io.File

object XmlParsing {

  fun parse(projectsDir : File) : TeamCityModel {
    val projects = projectsDir.listFiles { it ->
      it.isDirectory && !it.name.startsWith(".") && !it.name.startsWith("_")
    } ?: return TeamCityModel(listOf())

    val modelProjects = projects
            .asSequence()
            .map{ dir ->
              val projectConfig = dir / "project-config.xml"
              if (!projectConfig.isFile)
                null
              else
                parseProject(dir.name, projectConfig)
            }
            .filterNotNull()
            .toList()

    return TeamCityModel(modelProjects)
  }

  private fun parseProject(id : String, file : File) : TCProject {
    try {
      return parseProjectImpl(id, file)
    } catch (t : Throwable) {
      throw RuntimeException("Failed to parse $file for projectId=$id. ${t.message}", t)
    }
  }

  private fun parseProjectImpl(id : String, file : File) : TCProject {
    val raw = JDOM.load(file.loadJDOM().rootElement!!, TCProject::class.java)
    raw.id = id

    val buildsOrTemplates = (file.parentFile / "buildTypes")
            .listFiles { it -> it.isFile && it.name.endsWith(".xml") }
            ?.sorted()
            ?.map { parseBuildTypeOrTemplate( it.name.substring(0, it.name.length - 4), it)}
            ?: listOf()

    raw.buildTypes = buildsOrTemplates.map{it.buildType}.filterNotNull()
    raw.buildTemplates = buildsOrTemplates.map{it.buildTemplate }.filterNotNull()

    raw.vcsRoots = (file.parentFile / "vcsRoots")
            .listFiles { it -> it.isFile && it.name.endsWith(".xml") }
            ?.sorted()
            ?.map { parseVCSRoot( it.name.substring(0, it.name.length - 4), it)}
            ?: listOf()
    val pluginSettings = file.parentFile / "pluginData" / "plugin-settings.xml"
    raw.pluginSettings = if (pluginSettings.isFile) parsePluginSettings(pluginSettings) else null
    raw.metaRunners = (file.parentFile / "pluginData" / "metaRunners")
            .listFiles { it -> it.isFile && it.name.endsWith(".xml") }
            ?.sorted()
            ?.map { parseMetaRunner(it.name.substring(0, it.name.length -4), it) }
            ?: listOf()

    return raw
  }

  private data class BuildTypeOrTemplate(val buildType : TCBuildType? = null, val buildTemplate: TCBuildTemplate? = null)

  private fun parseBuildTypeOrTemplate(id : String, file : File) : BuildTypeOrTemplate {
    try {
      return parseBuildTypeOrTemplateImpl(id, file)
    } catch (t : Throwable) {
      throw RuntimeException("Failed to parse $file for buildType|buildTemplate=$id. ${t.message}", t)
    }
  }

  private fun parseBuildTypeOrTemplateImpl(id : String, file : File) : BuildTypeOrTemplate {
    val root = file.loadJDOM().rootElement!!

    if (root.name == TCBuildType::class.java.getAnnotationRec(XRoot::class.java)!!.name) {
      val raw = JDOM.load(root, TCBuildType::class.java)
      raw.id = id
      return BuildTypeOrTemplate(raw, null)
    } else if (root.name == TCBuildTemplate::class.java.getAnnotationRec(XRoot::class.java)!!.name){
      val raw = JDOM.load(root, TCBuildTemplate::class.java)
      raw.id = id
      return BuildTypeOrTemplate(null, raw)
    } else {
      throw RuntimeException("Unknown root element: " + root.name)
    }
  }

  private fun parseMetaRunner(id : String, file : File) : TCMetaRunner {
    try {
      val raw = JDOM.load(file.loadJDOM().rootElement!!, TCMetaRunner::class.java)
      raw.id = id
      return raw
    } catch (t : Throwable) {
      throw RuntimeException("Failed to parse $file for meta-runner=$id. ${t.message}", t)
    }
  }

  private fun parseVCSRoot(id : String, file : File) : TCVCSRoot {
    try {
      val raw = JDOM.load(file.loadJDOM().rootElement!!, TCVCSRoot::class.java)
      raw.id = id
      return raw
    } catch (t : Throwable) {
      throw RuntimeException("Failed to parse $file for vcsRoot=$id. ${t.message}", t)
    }
  }

  private fun parsePluginSettings(file : File) : TCPluginSettings {
    try {
      val raw = JDOM.load(file.loadJDOM().rootElement!!, TCPluginSettings::class.java)
      return raw
    } catch (t : Throwable) {
      throw RuntimeException("Failed to parse $file for plugin settings. ${t.message}", t)
    }
  }
}

fun File.loadJDOM() : Document {
  val builder = SAXBuilder(XMLReaders.NONVALIDATING, null, null)
  builder.setEntityResolver { a,b -> null }
  builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
  val doc = builder.build(this)!!
  return doc
}
