package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML


class TCProjectOrdering {
  private var rawProjectsOrder by JXML / "subprojectsOrder" / XAttribute("order")
  private var rawBuildsOrder by JXML / "buildTypesOrder" / XAttribute("order")

  var projectsOrder: List<String>?
    get() = rawProjectsOrder?.split(",")
    set(value) {
      rawProjectsOrder = value?.joinToString(",")
    }

  var buildsOrder: List<String>?
    get() = rawBuildsOrder?.split(",")
    set(value) {
      rawBuildsOrder = value?.joinToString(",")
    }
}

fun TCProjectOrdering?.isEmpty()
        = this == null || (buildsOrder?.isEmpty() ?: true && projectsOrder?.isEmpty() ?: true)
