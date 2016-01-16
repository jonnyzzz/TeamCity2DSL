package org.jonnyzzz.teamcity.dsl.api

import com.google.common.io.CharStreams
import org.jonnyzzz.teamcity.dsl.using
import java.io.InputStreamReader

val version : String by lazy(LazyThreadSafetyMode.NONE) {
  try {
    class M
    using(M::class.java.getResourceAsStream("/BUILD")!!) {
      CharStreams.toString(InputStreamReader(this, "utf-8"))?.trim()!!
    }
  } catch(t: Throwable) {
    "SNAPSHOT"
  }
}
