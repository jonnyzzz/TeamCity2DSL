package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.*
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML
import org.jonnyzzz.kotlin.xml.bind.jdom.XUnknown

interface TCProjectRef {
  val id : String?
}

@XRoot("project")
abstract class TCProject(override val id : String) : TCUUID, TCProjectRef {
  override var uuid by JXML[0x1000] / XAttribute("uuid") - null

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

  var projectExtensions by JXML / "project-extensions" / XSub(TCProjectExtensions::class.java)

  var cleanup by JXML / "cleanup" / XUnknown

  var ordering by JXML / XSub(TCProjectOrdering::class.java)
}
