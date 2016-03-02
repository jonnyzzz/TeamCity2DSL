package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.TeamCityVersion

interface GlobalSettingsBuilder {
  val v8 : TeamCityVersion
    get() = org.jonnyzzz.teamcity.dsl.model.TeamCityVersion.v8

  val v9 : TeamCityVersion
    get() = org.jonnyzzz.teamcity.dsl.model.TeamCityVersion.v9

  val v10 : TeamCityVersion
    get() = org.jonnyzzz.teamcity.dsl.model.TeamCityVersion.v10

  var TeamCityVersion : TeamCityVersion
}

interface GlobalSettingsResult


fun global(builder : GlobalSettingsBuilder.() -> Unit) : GlobalSettingsResult {
  org.jonnyzzz.teamcity.dsl.api.internal.DSLRegistry.addOnGlobalCallback(builder)

  return object : GlobalSettingsResult { }
}
