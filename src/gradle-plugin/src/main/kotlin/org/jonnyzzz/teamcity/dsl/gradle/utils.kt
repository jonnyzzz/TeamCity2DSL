package org.jonnyzzz.teamcity.dsl.gradle

import java.io.File

inline fun <R> ClassLoader.context(action : ClassLoader.() -> R) {
  val currentThread = Thread.currentThread()
  val loader = currentThread.contextClassLoader
  currentThread.contextClassLoader = this

  try {

    action()

  } finally {
    currentThread.contextClassLoader = loader
  }
}

operator fun File?.div(s : String) : File = File(this, s)
