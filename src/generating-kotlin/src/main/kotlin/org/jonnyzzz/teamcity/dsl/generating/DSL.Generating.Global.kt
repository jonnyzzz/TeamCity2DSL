package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.api.global
import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.model.TeamCityModel
import org.jonnyzzz.teamcity.dsl.writeUTF
import java.io.File

fun generateGlobal(context : GenerationContext, root : File, model : TeamCityModel, options : DSLOptions) {
  root.mkdirs()
  (root / "globals.kt").writeUTF {
    generateKotlinDSL(options.packageName, "globals") {
      appendln("@Suppress(\"unused\")")
      block("val globals = ${::global.name}") {
        appendln()
        appendln("TeamCityVersion = ${model.version.fieldName}")
      }
    }
  }
}
