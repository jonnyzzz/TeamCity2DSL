package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.TCParameter
import org.jonnyzzz.teamcity.dsl.model.TCSettingsTrigger

fun TCSettingsTrigger.param(name : String, value : String) {
  parameters = (parameters ?: listOf()) + TCParameter().apply {
    this.name = name
    this.value = value
  }
}

