package org.jonnyzzz.teamcity.dsl.api

import kotlin.collections.arrayListOf
import kotlin.collections.forEach
import kotlin.collections.toList

class LazyBuilders<T : Any>(builder : T.() -> Unit) : TCDSLLazy {
  private val builders = arrayListOf(builder)
  private var initialized = false

  var instance : T? = null

  fun add(builder : T.() -> Unit) {
    if (initialized)  {
      instance!!.builder()
    } else {
      builders.add(builder)
    }
  }

  override fun doLazyInit() {
    while(!builders.isEmpty()) {
      val toUpdate = builders.toList()
      builders.clear()
      toUpdate.forEach { instance!!.it() }
    }
    initialized = true
  }
}
