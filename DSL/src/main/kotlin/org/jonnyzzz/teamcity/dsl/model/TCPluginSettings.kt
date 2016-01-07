package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XAnyElements
import org.jonnyzzz.kotlin.xml.bind.XRoot
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML
import org.jonnyzzz.kotlin.xml.bind.jdom.XUnknown


@XRoot("settings")
class TCPluginSettings {
  var settings by JXML / XAnyElements / XUnknown
}

private class please_show_TCPluginData_class_with_kt_icon_in_idea
