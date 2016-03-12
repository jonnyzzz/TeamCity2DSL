package org.jonnyzzz.teamcity.dsl.model


import org.jonnyzzz.kotlin.xml.bind.*
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

class TCRequirement {
  var id by JXML / XAttribute("id") - null
  var type by JXML / XName
  var name by JXML / XAttribute("name")
  var value by JXML / XAttribute("value")
}
