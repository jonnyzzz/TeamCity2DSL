package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.model.TCID
import org.jonnyzzz.teamcity.dsl.model.TCUUID

fun KotlinWriter.uuid(it : TCUUID) {
  val uuid = it.uuid
  if (uuid != null) {
    appendln("uuid(${uuid.quote()})")
  }
}

fun KotlinWriter.id(it : TCID) {
  val id = it.id
  if (id != null) {
    appendln("id(${id.quote()})")
  }
}
