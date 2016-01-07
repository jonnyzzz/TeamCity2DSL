package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

abstract class TCID {
  var id : String? = null
}

abstract class TCUUID : TCID() {
  var uuid by JXML[0x1000] / XAttribute("uuid") - null
}
