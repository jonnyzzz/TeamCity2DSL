package org.jonnyzzz.teamcity.dsl.api.internal

import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.model.TCUUID
import java.util.*

interface DSLRegistryFacade {
  fun addCompletedProject(p: TCProject)
  fun addOnCompletedUUIDCallback(callback: (TCUUID) -> Unit)
}

internal object DSLRegistry : DSLRegistryFacade  {
  private val facade : Iterable<DSLRegistryFacade> by lazy {
    val result = ServiceLoader.load(DSLRegistryFacade::class.java).asIterable()

    if (!result.any()) {
      println("Detected implementation class: EMPTY")
    } else {
      result.forEach {
        println("Detected implementation class: ${it.javaClass.name}")
      }
    }

    result
  }

  private inline fun call(action : DSLRegistryFacade.() -> Unit) {
    facade.forEach { it.action() }
  }

  override fun addCompletedProject(p: TCProject): Unit {
    println("Registered project: ${p.id}")
    call { addCompletedProject(p) }
  }


  override fun addOnCompletedUUIDCallback(callback: (TCUUID) -> Unit) {
    println("Registered uuids callback")
    call { addOnCompletedUUIDCallback(callback) }
  }
}
