package org.jonnyzzz.teamcity.dsl.model


data class TeamCityModel(
        val version : TeamCityVersion,
        val projects: List<TCProject>
) {

  val size : Int
    get() = projects.size
}

sealed class TeamCityVersion(val version : String) {
  object v8 : TeamCityVersion("8.0")

  abstract class XSDTarget(version : String, val schemaLocation : String) : TeamCityVersion(version) {
    companion object {
      val namespaceURI = "http://www.w3.org/2001/XMLSchema-instance"
      val namespacePrefix = "xsi"
      val namespaceAttribute = "noNamespaceSchemaLocation"

      val values : List<XSDTarget>
        get() = listOf(v9, v10)
    }
  }

  object v9 : XSDTarget("9.0", "http://www.jetbrains.com/teamcity/schemas/9.0/project-config.xsd")
  object v10 : XSDTarget("10.0", "http://www.jetbrains.com/teamcity/schemas/10.0/project-config.xsd")

  companion object {
    val latest : TeamCityVersion
      get() = v10
  }
}
