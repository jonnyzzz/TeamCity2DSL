package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.TCBuildSettings


fun TCBuildSettings.disable(id : String) {
    disabledSettings = (disabledSettings ?: listOf()) + id
}

fun TCBuildSettings.cleanup(builder : TCUnknownBuilder.() -> Unit) {
    cleanup = elementImpl("cleanup", builder)
}
