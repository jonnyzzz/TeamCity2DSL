package org.jonnyzzz.teamcity.dsl.gradle

import org.w3c.dom.Element
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

object TeamCity2DSLPlugin {
  val DSL_PLUGIN_CLASSPATH by lazy { System.getProperty("TEST_PLUGIN_CLASSPATH")!! }
  val DSL_PLUGIN_NAME by lazy { System.getProperty("TEST_PLUGIN_NAME")!! }

  val DSL_PLUGIN_LATEST_PUBLIC_VERSION by lazy {
    val xml = URL("http://dl.bintray.com/jonnyzzz/maven/org/jonnyzzz/teamcity/dsl/gradle-plugin/maven-metadata.xml")
    .openStream().use { it.readBytes(1024 * 1024) }

    val dbFactory = DocumentBuilderFactory.newInstance();
    val dBuilder = dbFactory.newDocumentBuilder();
    val doc = dBuilder.parse(xml.inputStream());

    fun Element.lastElementOrNull(name: String) : Element? {
      val nodes = getElementsByTagName(name)
      for(i in (0 .. nodes.length-1).reversed()) {
        val node = nodes.item(i)
        if (node is Element) return node
      }
      return null
    }


    doc.documentElement.lastElementOrNull("versioning")?.lastElementOrNull("versions")?.lastElementOrNull("version")?.textContent
  }

  val scriptHeader by lazy {
    """
    buildscript {
      repositories {
        mavenLocal()
        mavenCentral()
      }

      dependencies {
        classpath '${DSL_PLUGIN_CLASSPATH}'
      }
    }

    apply plugin: '${DSL_PLUGIN_NAME}'

   """
  }
}

