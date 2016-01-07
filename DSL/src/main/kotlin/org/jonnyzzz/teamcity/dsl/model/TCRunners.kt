package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.XElements
import org.jonnyzzz.kotlin.xml.bind.XRoot
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML
import kotlin.collections.listOf


interface TCSettingsRunnerRef {
  val id : String?
}

@XRoot("runner")
open class TCSettingsRunner : TCSettingsRunnerRef {
  override var id by JXML / XAttribute("id")
  var name by JXML / XAttribute("name")
  var runnerType by JXML / XAttribute("type")
  var parameters by JXML / "parameters" / XElements("param") / XSub(TCParameter::class.java) - listOf()
}
