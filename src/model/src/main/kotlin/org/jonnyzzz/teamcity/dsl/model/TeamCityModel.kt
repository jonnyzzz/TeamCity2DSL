package org.jonnyzzz.teamcity.dsl.model


data class TeamCityModel(
        val version : TeamCityVersion,
        val projects: List<TCProject>
) {

  val size : Int
    get() = projects.size
}

sealed class TeamCityVersion(val fieldName: String, val version : String) {
  object v8 : TeamCityVersion("v8", "8.0")

  abstract class XSDTarget(fieldName : String, version : String, val schemaLocation : String) : TeamCityVersion(fieldName, version) {
    companion object {
      val namespaceURI = "http://www.w3.org/2001/XMLSchema-instance"
      val namespacePrefix = "xsi"
      val namespaceAttribute = "noNamespaceSchemaLocation"

      val values : List<XSDTarget>
        get() = listOf(v9, v10)
    }
  }

  object v9 : XSDTarget("v9", "9.0", "http://www.jetbrains.com/teamcity/schemas/9.0/project-config.xsd")
  object v10 : XSDTarget("v10", "10.0", "http://www.jetbrains.com/teamcity/schemas/10.0/project-config.xsd")

  companion object {
    val latest : TeamCityVersion
      get() = v10
  }
}
