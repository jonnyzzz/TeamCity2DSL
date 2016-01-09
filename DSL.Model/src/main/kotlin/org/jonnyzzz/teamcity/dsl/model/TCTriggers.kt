package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.XElements
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

interface TCSettingsTriggerRef {
  val id : String?
}

class TCSettingsTrigger : TCSettingsTriggerRef {
  override var id by JXML / XAttribute("id")
  var triggerType by JXML / XAttribute("type")
  var parameters by JXML / "parameters" / XElements("param") / XSub(TCParameter::class.java) - listOf()
}
