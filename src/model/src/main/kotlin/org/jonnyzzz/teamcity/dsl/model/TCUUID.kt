package org.jonnyzzz.teamcity.dsl.model

interface TCID {
  val id : String
}

interface TCUUID : TCID {
  var uuid : String?
}
