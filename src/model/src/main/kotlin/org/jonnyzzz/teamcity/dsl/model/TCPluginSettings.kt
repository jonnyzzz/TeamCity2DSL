package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.*
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML
import org.jonnyzzz.kotlin.xml.bind.jdom.XUnknown


@XRoot("settings")
class TCPluginSettings {
  var settings by JXML / XAnyElements / XUnknown
}

@XRoot("project-extensions")
class TCProjectExtensions {
  var extensions by JXML / XElements("extension") / XSub(TCProjectExtension::class.java)
}

@XRoot("extension")
class TCProjectExtension {
  var id by JXML / XAttribute("id")
  var type by JXML / XAttribute("type")

  var parameters by JXML / "parameters" / XElements("param") / XSub(TCParameter::class.java) - listOf()
}
