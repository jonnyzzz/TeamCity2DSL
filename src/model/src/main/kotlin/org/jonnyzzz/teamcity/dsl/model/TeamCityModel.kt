package org.jonnyzzz.teamcity.dsl.model


data class TeamCityModel(
        val projects: List<TCProject>
) {

  val size : Int
    get() = projects.size
}
