package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.model.TCAbstractParam
import org.jonnyzzz.teamcity.dsl.model.TCParameter
import org.jonnyzzz.teamcity.dsl.model.TCParameterWithSpec
import kotlin.collections.*
import kotlin.text.contains
import kotlin.text.split
import kotlin.text.toRegex


fun KotlinWriter.paramsWithSpec(parameters: List<TCParameterWithSpec>?) {
  params(parameters) {
    val spec = it.spec
    val f : KotlinWriter.() -> Unit = { setter("spec", spec) }
    if (spec == null) null else f
  }
}

fun <T : TCParameter> KotlinWriter.params(parameters: List<T>?,
                                          defaultParams: List<T>? = listOf(),
                                          expand: (T) -> (KotlinWriter.() -> Unit)? = { null }) {
  if (parameters == null) return

  val actualParameters = parameters.filter { p -> null == defaultParams?.firstOrNull { d -> d.name == p.name && d.value == p.value } }
  abstractParams(actualParameters, "param", expand = {
    val theirs = expand(it)

    val expandText : KotlinWriter.() -> Unit = {
      appendln("legacyText()")
      if (theirs != null) theirs()
    }

    if (it.escapeAsText) expandText else theirs
  })
}

fun String.splitNewLines(sep : String = "\n") : List<String> = split(sep.toRegex()).toTypedArray().toList()

fun <T : TCAbstractParam> KotlinWriter.abstractParams(parameters: List<T>?,
                                                      functionName: String,
                                                      expand: (T) -> (KotlinWriter.() -> Unit)? = { null },
                                                      customRender: (T) -> (KotlinWriter.() -> Unit)? = { null }) {

  parameters
          ?.filter { it.name != null && it.value != null }
          ?.forEach {
            val customRenderFn = customRender(it)

            if (customRenderFn != null) {
              customRenderFn()
            } else {
              val qname = it.name?.quote()
              val value = it.value ?: ""
              val expanded = expand(it)

              if (value.contains("\n") || expanded != null) {
                block("$functionName(${qname})") {
                  if (expanded != null) expanded()

                  value.splitNewLines().forEach {
                    appendln("value += ${it.quoteWithRefs()}")
                  }
                }
              } else {
                appendln("$functionName(${qname}, ${it.value?.quoteWithRefs()})")
              }
            }
          }
}
