package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.*
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML
import org.jonnyzzz.kotlin.xml.bind.jdom.XUnknown
import kotlin.collections.listOf

interface TCProjectRef {
  val id : String?
}

@XRoot("project")
open class TCProject : TCUUID(), TCProjectRef {
  var buildTemplates = listOf<TCBuildTemplate>()
  var buildTypes = listOf<TCBuildType>()
  var vcsRoots = listOf<TCVCSRoot>()
  var metaRunners = listOf<TCMetaRunner>()
  var pluginSettings = null as TCPluginSettings?

  var parentId by JXML[0x800] / XAttribute("parent-id")
  var archived by JXML[0x801] / XAttribute("archived")
  var name  by JXML / "name" / XText
  var description  by JXML / "description" / XText
  var parameters by JXML / "parameters" / XElements("param") / XSub(TCParameterWithSpec::class.java) - listOf()

  var cleanup by JXML / "cleanup" / XUnknown
}



private class please_show_TCProject_class_with_kt_icon_in_idea
