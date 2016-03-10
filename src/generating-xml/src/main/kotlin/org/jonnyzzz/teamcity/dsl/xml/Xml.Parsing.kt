package org.jonnyzzz.teamcity.dsl.xml

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
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
    } ?: arrayOf()

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

    if (modelProjects.isEmpty()) {
       return TeamCityModel(TeamCityVersion.latest, listOf())
    }

    val versions = modelProjects.map { it.version }.distinct()
    if (versions.size != 1) {
      throw Error("Only one version of files is allowed, but were: $versions")
    }

    return TeamCityModel(versions.single(), modelProjects.map { it.project }.toList())
  }

  private fun parseTeamCityTargetVersion(rootElement: Element) : TeamCityVersion {
    val schema = rootElement.getAttribute(
            TeamCityVersion.XSDTarget.namespaceAttribute,
            Namespace.getNamespace(TeamCityVersion.XSDTarget.namespaceURI))
            ?.value
            ?: return TeamCityVersion.v8

    return TeamCityVersion.XSDTarget.values.firstOrNull { it.schemaLocation == schema }
            ?: throw Error("Failed to resolve TeamCity target version from scheme: $schema")
  }

  private fun assertTeamCityTargetVersion(version : TeamCityVersion, rootElement: Element) {
    val thisFileVersion = parseTeamCityTargetVersion(rootElement)
    if (version != thisFileVersion) {
      throw Error("Expected version $version but was $thisFileVersion")
    }
  }

  private fun parseProject(id : String, file : File) : TCProjectAndVersion {
    try {
      return parseProjectImpl(id, file)
    } catch (t : Throwable) {
      throw RuntimeException("Failed to parse $file for projectId=$id. ${t.message}", t)
    }
  }

  private data class TCProjectAndVersion(val version : TeamCityVersion, val project : TCProject)

  private fun parseProjectImpl(id : String, file : File) : TCProjectAndVersion {
    val rootElement = file.loadJDOM().rootElement!!
    val version = parseTeamCityTargetVersion(rootElement)
    val raw = object:TCProject(id) { }.bind(rootElement)

    val buildsOrTemplates = (file.parentFile / "buildTypes")
            .listFiles { it -> it.isFile && it.name.endsWith(".xml") }
            ?.sorted()
            ?.map { parseBuildTypeOrTemplate( version, it.name.substring(0, it.name.length - 4), it)}
            ?: listOf()

    raw.buildTypes = buildsOrTemplates.map{it.buildType}.filterNotNull()
    raw.buildTemplates = buildsOrTemplates.map{it.buildTemplate }.filterNotNull()

    raw.vcsRoots = (file.parentFile / "vcsRoots")
            .listFiles { it -> it.isFile && it.name.endsWith(".xml") }
            ?.sorted()
            ?.map { parseVCSRoot( version,  it.name.substring(0, it.name.length - 4), it)}
            ?: listOf()
    val pluginSettings = file.parentFile / "pluginData" / "plugin-settings.xml"
    raw.pluginSettings = if (pluginSettings.isFile) parsePluginSettings(pluginSettings) else null
    raw.metaRunners = (file.parentFile / "pluginData" / "metaRunners")
            .listFiles { it -> it.isFile && it.name.endsWith(".xml") }
            ?.sorted()
            ?.map { parseMetaRunner(it.name.substring(0, it.name.length -4), it) }
            ?: listOf()

    return TCProjectAndVersion(version, raw)
  }

  private data class BuildTypeOrTemplate(val buildType : TCBuildType? = null, val buildTemplate: TCBuildTemplate? = null)

  private fun parseBuildTypeOrTemplate(version : TeamCityVersion, id : String, file : File) : BuildTypeOrTemplate {
    try {
      return parseBuildTypeOrTemplateImpl(version, id, file)
    } catch (t : Throwable) {
      throw RuntimeException("Failed to parse $file for buildType|buildTemplate=$id. ${t.message}", t)
    }
  }

  private fun parseBuildTypeOrTemplateImpl(version : TeamCityVersion, id : String, file : File) : BuildTypeOrTemplate {
    val root = file.loadJDOM().rootElement!!

    assertTeamCityTargetVersion(version, root)

    if (root.name == TCBuildType::class.java.getAnnotationRec(XRoot::class.java)!!.name) {
      val raw = object:TCBuildType(id) { }.bind(root)
      return BuildTypeOrTemplate(raw, null)
    } else if (root.name == TCBuildTemplate::class.java.getAnnotationRec(XRoot::class.java)!!.name){
      val raw = object:TCBuildTemplate(id){ }.bind(root)
      return BuildTypeOrTemplate(null, raw)
    } else {
      throw RuntimeException("Unknown root element: " + root.name)
    }
  }

  private fun parseMetaRunner(id : String, file : File) : TCMetaRunner {
    try {
      return object:TCMetaRunner(id){ }.bind(file.loadJDOM().rootElement!!)
    } catch (t : Throwable) {
      throw RuntimeException("Failed to parse $file for meta-runner=$id. ${t.message}", t)
    }
  }

  private fun parseVCSRoot(version : TeamCityVersion, id : String, file : File) : TCVCSRoot {
    try {
      val rootElement = file.loadJDOM().rootElement!!

      assertTeamCityTargetVersion(version, rootElement)

      return object:TCVCSRoot(id){ }.bind(rootElement)
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
