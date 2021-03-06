package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.XRoot
import org.jonnyzzz.kotlin.xml.bind.XText
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

interface TCBuildTemplateRef {
  val id : String?
}

@XRoot("template")
abstract class TCBuildTemplate(override val id : String) : TCBuildSettings(), TCUUID, TCBuildOrTemplate, TCBuildTemplateRef {
  override var uuid by JXML[0x1000] / XAttribute("uuid") - null

  var name by JXML / "name" / XText
  var description by JXML / "description" / XText
}
