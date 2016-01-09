package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XRoot
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.XText
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

interface TCBuildTemplateRef {
  val id : String?
}

@XRoot("template")
open class TCBuildTemplate : TCBuildOrTemplate(), TCWithSettings, TCBuildTemplateRef {
  var name by JXML / "name" / XText
  var description by JXML / "description" / XText
  private var _settings by JXML / "settings" / XSub(TCBuildTemplateSettings::class.java) - TCBuildTemplateSettings()


  override val settings: TCBuildTemplateSettings
    get() {
      val s = _settings ?: TCBuildTemplateSettings()
      _settings = s
      return s
    }
}