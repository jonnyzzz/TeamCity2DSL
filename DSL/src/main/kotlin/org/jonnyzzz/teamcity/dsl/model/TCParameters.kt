package org.jonnyzzz.teamcity.dsl.model

import org.jdom2.CDATA
import org.jdom2.Element
import org.jonnyzzz.kotlin.xml.bind.*
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML
import org.jonnyzzz.kotlin.xml.bind.jdom.XUnknown
import org.jonnyzzz.teamcity.dsl.generating.quote
import kotlin.collections.any
import kotlin.text.contains
import kotlin.text.isNotEmpty
import kotlin.text.trim

interface TCAbstractParam {
  var name : String?
  var value : String?
}

@XRoot("param")
open class TCParameter : TCAbstractParam {
  override var name : String? by JXML[1] / XAttribute("name")

  var escapeAsText : Boolean
    get() = actualEscapeAsText
    set(v) {
      actualEscapeAsText = v
      updateValue()
    }

  override var value: String?
    get() = actualValue
    set(v: String?) {
      actualValue = v
      updateValue()
    }

  private fun updateValue() {
    val v = value
    when {
      v == null -> {
        valueAttribute = null
        valueCDATA = null
        valueText = null
      }
      !v.contains("\n") && !v.contains("\r") -> {
        valueAttribute = v
        valueCDATA = null
        valueText = null
      }
      else -> {
        valueAttribute = null
        valueCDATA = if (!escapeAsText) v else null
        valueText = if (escapeAsText) v else null
      }
    }
  }

  private var valueAttribute by JXML[2] / XAttribute("value")
  private var valueCDATA by JXML[10] / XCDATA
  private var valueText by JXML[11] / XText
  private var valueRaw by JXML[442] / XUnknown - XReadOnly - XCallback<Element>(onLoaded = { E -> onLoaded(E); E})

  private var actualValue : String? = null
  private var actualEscapeAsText : Boolean = false

  private fun onLoaded(valueRaw : Element?) {
    actualEscapeAsText = valueAttribute == null && !(valueRaw?.content?.any { it is CDATA && it.textTrim?.trim()?.isNotEmpty() ?: false } ?: false)
    value = valueAttribute ?: valueCDATA ?: valueText
  }

  override fun toString(): String {
    return "param(${(name?:"").quote()}, ${(value?:"").quote()})";
  }
}

open class TCParameterWithSpec : TCParameter() {
  var spec by JXML[3] / XAttribute("spec")
}
