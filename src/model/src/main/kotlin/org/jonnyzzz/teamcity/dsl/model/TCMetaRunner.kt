package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.XRoot
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.XText
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

interface TCMetaRunnerRef {
  val id : String?
}

@XRoot("meta-runner")
abstract class TCMetaRunner(override val id : String) : TCMetaRunnerSettings(), TCID, TCWithSettings, TCMetaRunnerRef {
  var name by JXML[0x100] / XAttribute("name")
  var description by JXML[0x200] / "description" / XText - ""

  override val settings = this
}
