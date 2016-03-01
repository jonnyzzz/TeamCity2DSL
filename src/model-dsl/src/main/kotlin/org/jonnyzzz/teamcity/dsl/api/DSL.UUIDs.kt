package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.*


interface TCUUIDs {
  infix fun String.to(uuid : String)
  infix fun TCVCSRootRef.to(uuid : String)
  infix fun TCProjectRef.to(uuid : String)
  infix fun TCBuildTypeRef.to(uuid : String)
  infix fun TCBuildTemplateRef.to(uuid : String)

  operator fun String.plusAssign(uuid: String)
  operator fun TCVCSRootRef.plusAssign(uuid : String)
  operator fun TCProjectRef.plusAssign(uuid : String)
  operator fun TCBuildTypeRef.plusAssign(uuid : String)
  operator fun TCBuildTemplateRef.plusAssign(uuid : String)
}

fun uuids(builder : TCUUIDs.() -> Unit) : TCUUIDs = object : TCUUIDs {
  override fun String.to(uuid : String) { callback { if (it.id == this) it.uuid = uuid } }
  override fun TCVCSRootRef.to(uuid : String) { callback { if (it is TCVCSRoot && it.id == this.id) it.uuid = uuid } }
  override fun TCProjectRef.to(uuid : String) { callback { if (it is TCProject && it.id == this.id) it.uuid = uuid } }
  override fun TCBuildTypeRef.to(uuid : String) { callback { if (it is TCBuildType && it.id == this.id) it.uuid = uuid } }
  override fun TCBuildTemplateRef.to(uuid : String) { callback { if (it is TCBuildTemplate && it.id == this.id) it.uuid = uuid } }

  override fun String.plusAssign(uuid: String) { this to uuid }
  override fun TCVCSRootRef.plusAssign(uuid : String) { this to uuid }
  override fun TCProjectRef.plusAssign(uuid : String) { this to uuid }
  override fun TCBuildTypeRef.plusAssign(uuid : String) { this to uuid }
  override fun TCBuildTemplateRef.plusAssign(uuid : String) { this to uuid }

  private fun callback(callback: (TCUUID) -> Unit) {
    org.jonnyzzz.teamcity.dsl.api.internal.DSLRegistry.addOnCompletedUUIDCallback(callback)
  }
}.apply { builder() }
