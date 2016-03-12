package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.XRoot
import org.jonnyzzz.kotlin.xml.bind.XText
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

interface TCBuildTypeRef {
  val id : String?
}

@XRoot("build-type")
abstract class TCBuildType(override val id : String) : TCBuildSettings(), TCUUID, TCBuildOrTemplate, TCBuildTypeRef {
  override
  var uuid by JXML[0x1000] / XAttribute("uuid") - null

  var paused by JXML[0x1100] / XAttribute("paused")

  var name by JXML[0x100] / "name" / XText
  var description by JXML[0x200] / "description" / XText - ""

  var templateId : String?
    get() = templateIdImpl
    set(value) {
      templateIdImpl = value
      updateOrderAttribute()
    }

  var runnersOrder : List<String>?
    get() = runnersOrderImpl?.let { it.split(", ".toRegex()).toTypedArray() }?.toList()
    set(value) {
      runnersOrderCache = value
      updateOrderAttribute()
    }

  private fun updateOrderAttribute() {
    if (templateId != null) {
      runnersOrderImpl = runnersOrderCache?.joinToString(", ")
    } else {
      runnersOrderImpl = null
    }
  }

  private var runnersOrderCache : List<String>? = null
  private var runnersOrderImpl by JXML[0xc404] / "settings" / "build-runners" / XAttribute("order")
  private var templateIdImpl by JXML[0xc400] / "settings" / XAttribute("ref")
}
