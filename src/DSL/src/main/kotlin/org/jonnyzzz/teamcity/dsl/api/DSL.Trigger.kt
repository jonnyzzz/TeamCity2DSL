package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.having
import org.jonnyzzz.teamcity.dsl.model.TCParameter
import org.jonnyzzz.teamcity.dsl.model.TCSettingsTrigger
import kotlin.collections.listOf
import kotlin.collections.plus

fun TCSettingsTrigger.param(name : String, value : String) {
  parameters = (parameters ?: listOf()) + having(TCParameter()) {
    this.name = name
    this.value = value
  }
}

