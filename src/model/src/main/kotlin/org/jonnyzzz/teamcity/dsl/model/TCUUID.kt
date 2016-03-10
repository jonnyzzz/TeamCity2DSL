package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

interface TCID {
  val id : String
}

interface ITCUUID : TCID {
  val uuid : String?
}

abstract class TCUUID : TCID, ITCUUID {
  override var uuid by JXML[0x1000] / XAttribute("uuid") - null
}
