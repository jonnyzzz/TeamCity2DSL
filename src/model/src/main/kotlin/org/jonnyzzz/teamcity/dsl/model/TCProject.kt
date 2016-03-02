package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.*
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML
import org.jonnyzzz.kotlin.xml.bind.jdom.XUnknown

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

  var ordering by JXML / XSub(TCProjectOrdering::class.java)
}

class TCProjectOrdering {
  private var rawProjectsOrder by JXML / "subprojectsOrder" / XAttribute("order")
  private var rawBuildsOrder by JXML / "buildTypesOrder" / XAttribute("order")

  var projectsOrder: List<String>?
    get() = rawProjectsOrder?.split(",")
    set(value) {
      rawProjectsOrder = value?.joinToString(",")
    }

  var buildsOrder: List<String>?
    get() = rawBuildsOrder?.split(",")
    set(value) {
      rawBuildsOrder = value?.joinToString(",")
    }
}

fun TCProjectOrdering?.isEmpty() = this == null || (buildsOrder?.isEmpty() ?: true && projectsOrder?.isEmpty() ?: true)
