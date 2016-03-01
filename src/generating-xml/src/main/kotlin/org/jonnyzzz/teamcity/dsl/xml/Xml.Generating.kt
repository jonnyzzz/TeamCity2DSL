package org.jonnyzzz.teamcity.dsl.xml

import org.jdom2.DocType
import org.jdom2.Document
import org.jdom2.Namespace
import org.jonnyzzz.kotlin.xml.bind.XRoot
import org.jonnyzzz.kotlin.xml.bind.jdom.JDOM
import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.getAnnotationRec
import org.jonnyzzz.teamcity.dsl.model.TCBuildOrTemplate
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.model.TCUUID
import org.jonnyzzz.teamcity.dsl.using
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object XmlGenerating {
  fun generate(projects : List<TCProject>, file : File) {
    file.mkdirs()

    if (!file.isDirectory) throw Error("Failed to cleanup destination folder")

    for (project in projects) {
      generateProject(file, project)
    }
  }

  private fun Document.initializeDocument(uuid : TCUUID) {
    when {
      uuid.uuid == null && uuid is TCProject -> {
        // TeamCity 8.1.x & older integration tests
        docType = DocType(uuid.javaClass.getAnnotationRec(XRoot::class.java)!!.name, "../../project-config.dtd")
      }
      uuid.uuid == null && uuid !is TCProject -> {
        // TeamCity 8.1.x & older integration tests
        docType = DocType(uuid.javaClass.getAnnotationRec(XRoot::class.java)!!.name, "../../../project-config.dtd")
      }
      else -> {
        val rootElement = rootElement
        val xsiNs = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")

        rootElement.addNamespaceDeclaration(xsiNs);
        rootElement.setAttribute("noNamespaceSchemaLocation", "http://www.jetbrains.com/teamcity/schemas/9.0/project-config.xsd", xsiNs);
      }
    }
  }

  private fun generateProject(root: File, project : TCProject) {
    val projectId = project.id ?: throw Error("Project should have an id")

    val home = root / projectId
    generateXML(home, "project-config.xml", project) { initializeDocument(project)}

    val buildAndTemplates : List<TCBuildOrTemplate> = project.buildTypes + project.buildTemplates
    if (buildAndTemplates.any()) {
      val buildTypesHome = home / "buildTypes"
      buildAndTemplates.forEach {
        generateXML(buildTypesHome, it.id + ".xml", it) { initializeDocument(it) }
      }
    }

    if (project.vcsRoots.any()) {
      val buildTypesHome = home / "vcsRoots"
      project.vcsRoots.forEach {
        generateXML(buildTypesHome, it.id + ".xml", it) { initializeDocument(it) }
      }
    }

    val pluginSettings = project.pluginSettings
    if (pluginSettings != null) {
      val buildTypesHome = home / "pluginData"
      generateXML(buildTypesHome, "plugin-settings.xml", pluginSettings) { }
    }

    project.metaRunners.forEach { meta ->
      generateXML(home / "pluginData" / "metaRunners", meta.id + ".xml", meta) { }
    }
  }

  private fun <T : Any> generateXML(home : File, fileName : String, obj : T, postProcess : Document.() -> Unit = {}) {
    home.mkdirs()
    val dst = home / fileName

    val doc = JDOM.save(obj).toDocument(postProcess)
    using(OutputStreamWriter(FileOutputStream(dst), "utf-8")) {
      xmlOutputter().output(doc, this);
    }
  }
}
