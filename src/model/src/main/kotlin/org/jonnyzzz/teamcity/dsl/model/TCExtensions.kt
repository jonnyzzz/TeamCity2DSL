package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.XElements
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

interface TCSettingsExtensionRef {
  val id : String?
}

class TCSettingsExtension : TCSettingsExtensionRef {
  override var id by JXML / XAttribute("id")
  var extensionType by JXML / XAttribute("type")
  var parameters by JXML / "parameters" / XElements("param") / XSub(TCParameter::class.java) - listOf()
}
