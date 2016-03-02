package org.jonnyzzz.teamcity.dsl.generating

import org.jonnyzzz.teamcity.dsl.api.TCRequirementsBuilder
import org.jonnyzzz.teamcity.dsl.api.TCRequirementsBuilderNames
import org.jonnyzzz.teamcity.dsl.api.TCRequirementsBuilderRequirement
import org.jonnyzzz.teamcity.dsl.model.TCRequirement
import org.jonnyzzz.teamcity.dsl.suppressing
import java.util.*
import kotlin.collections.forEach
import kotlin.collections.isEmpty
import kotlin.collections.toSortedSet

val wellknownRequirementNames = {
  val r = object : TCRequirementsBuilderNames { }

  val names = TreeSet<String>()
  r.javaClass.methods.forEach {
    suppressing {
      if (it.parameterTypes.isEmpty() && it.returnType == String::class.java) {
        names.add(it.invoke(r) as String)
      }
    }
  }

  names.toSortedSet()
}()

fun KotlinWriter.generateRequirements(requirements : List<TCRequirement>?) {
  if (requirements == null) return

  block("requirements") {
    requirements.forEach { generateRequirement(it) }
  }
}

fun KotlinWriter.generateRequirement(req: TCRequirement) {
  val id = req.id
  val type = req.type!!
  val name = req.name!!
  val value = req.value

  val args = id?.let { "(${it.quote()})"} ?: ""

  block("rule$args") {
    appendln(
            StringBuilder().apply {
              append("ref(${name.quote()}) - ")

              if (wellknownRequirementNames.contains(type)) {
                append("`$type`")
              } else {
                append("${type.quote()}")
              }

              if (value != null) {
                append(" - ${value.quote()}")
              }
            }.toString()

    )
  }
}