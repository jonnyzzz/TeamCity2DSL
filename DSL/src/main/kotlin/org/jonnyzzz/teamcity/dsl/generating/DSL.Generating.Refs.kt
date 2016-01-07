package org.jonnyzzz.teamcity.dsl.generating

import kotlin.text.*

fun String.quoteWithRefs() : String {
  if (this.length > 2 && this.startsWith("%") && this.endsWith("%")) {
    val r = this.substring(1, this.length - 1)
    if ("\"$r\"" == r.quote() && !r.contains("%")) return "ref(${r.quote()})"
  }

  val sep = '%'

  val result = StringBuilder()
  val buff = StringBuilder()
  var isInsideRef = false
  this.quote().forEach {
    if (it == sep) {
      if (isInsideRef) {
        if (buff.length == 0) {
          result.append(sep).append(sep)
        } else {
          result.append("\${ref(\"${buff.toString()}\")}")
        }
        isInsideRef = false
        buff.setLength(0)
      } else {
        isInsideRef = true
      }
    } else {
      if (isInsideRef)
        buff.append(it)
      else
        result.append(it)
    }
  }

  if (isInsideRef) {
    result.append(sep).append(buff)
  }

  return result.toString()
}
