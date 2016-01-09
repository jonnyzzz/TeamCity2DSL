package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.XElements
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML


class TCSettingsOptions {
  var options by JXML / XElements("option") / XSub(TCSettingsOption::class.java)
}

class TCSettingsOption : TCAbstractParam {
  override var name by JXML / XAttribute("name")
  override var value  by JXML / XAttribute("value")
}

