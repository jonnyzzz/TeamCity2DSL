package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.TCBuildSettings
import org.jonnyzzz.teamcity.dsl.model.TCWithSettings
import kotlin.collections.listOf
import kotlin.collections.plus

fun TCWithSettings.settings(builder : TCBuildSettings.() -> Unit) {
  settings.builder()
}

fun TCWithSettings.disable(id : String) {
  settings {
    disabledSettings = (disabledSettings ?: listOf()) + id
  }
}

fun TCWithSettings.cleanup(builder : TCUnknownBuilder.() -> Unit) {
  settings {
    cleanup = elementImpl("cleanup", builder)
  }
}
