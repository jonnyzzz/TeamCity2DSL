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
open class TCBuildType : TCBuildOrTemplate(), TCWithSettings, TCBuildTypeRef {
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

private class please_show_TCBuildType_class_with_kt_icon_in_idea
