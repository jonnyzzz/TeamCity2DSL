package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.*


open class TCUUIDs {

  private fun callback(callback : (TCUUID) -> Unit) {
    org.jonnyzzz.teamcity.dsl.api.internal.DSLRegistry.addOnCompletedUUIDCallback(callback)
  }

  infix fun String.to(uuid : String) { callback { if (it.id == this) it.uuid = uuid } }
  infix fun TCVCSRootRef.to(uuid : String) { callback { if (it is TCVCSRoot && it.id == this.id) it.uuid = uuid } }
  infix fun TCProjectRef.to(uuid : String) { callback { if (it is TCProject && it.id == this.id) it.uuid = uuid } }
  infix fun TCBuildTypeRef.to(uuid : String) { callback { if (it is TCBuildType && it.id == this.id) it.uuid = uuid } }
  infix fun TCBuildTemplateRef.to(uuid : String) { callback { if (it is TCBuildTemplate && it.id == this.id) it.uuid = uuid } }

  operator fun String.plusAssign(uuid: String) { this to uuid }
  operator fun TCVCSRootRef.plusAssign(uuid : String) { this to uuid }
  operator fun TCProjectRef.plusAssign(uuid : String) { this to uuid }
  operator fun TCBuildTypeRef.plusAssign(uuid : String) { this to uuid }
  operator fun TCBuildTemplateRef.plusAssign(uuid : String) { this to uuid }
}

inline fun uuids(builder : TCUUIDs.() -> Unit) : TCUUIDs = object : TCUUIDs() {}.apply { builder() }
