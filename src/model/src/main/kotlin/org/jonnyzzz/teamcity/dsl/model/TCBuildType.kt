package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.XRoot
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.XText
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

interface TCBuildTypeRef {
  val id : String?
}

@XRoot("build-type")
abstract class TCBuildType(override val id : String) : TCUUID, TCBuildOrTemplate, TCWithSettings, TCBuildTypeRef {
  override var uuid by JXML[0x1000] / XAttribute("uuid") - null

  var paused by JXML[0x1100] / XAttribute("paused")

  var name by JXML[0x100] / "name" / XText
  var description by JXML[0x200] / "description" / XText - ""
  private var _settings by JXML[0x300] / "settings" / XSub(TCBuildTypeSettings::class.java) - TCBuildTypeSettings()

  override val settings: TCBuildTypeSettings
    get() {
      val s = _settings ?: TCBuildTypeSettings()
      _settings = s
      return s
    }
}
