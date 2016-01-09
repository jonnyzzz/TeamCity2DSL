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
open class TCMetaRunner : TCID(), TCWithSettings, TCMetaRunnerRef {
  var name by JXML[0x100] / XAttribute("name")
  var description by JXML[0x200] / "description" / XText - ""

  private var _settings by JXML[0x300] / "settings" / XSub(TCMetaRunnerSettings::class.java) - TCMetaRunnerSettings()

  override val settings: TCMetaRunnerSettings
    get() {
      val s = _settings ?: TCMetaRunnerSettings()
      _settings = s
      return s
    }
}
