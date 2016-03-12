package org.jonnyzzz.teamcity.dsl.api


interface TCDSLLazy {
  fun doLazyInit()
}

internal class LazyBuilders<T : Any>(builder : T.() -> Unit) : TCDSLLazy {
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
