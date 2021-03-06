package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.TCBuildSettings
import org.jonnyzzz.teamcity.dsl.model.TCRequirement
import kotlin.collections.listOf
import kotlin.collections.plus


interface TCRequirementsBuilderRequirementValue {
  operator fun minus(value : String?)
}

interface TCRequirementsBuilderRequirement {
  operator fun minus(type: String): TCRequirementsBuilderRequirementValue
}

interface TCRequirementsBuilderRequirementRef {
  fun ref(param : String) : TCRequirementsBuilderRequirement
}

interface TCRequirementsBuilder : TCRequirementsBuilderNames {
  fun rule(id: String? = null, builder : TCRequirementsBuilderRequirementRef.() -> Unit)
}

interface TCRequirementsBuilderNames {
  val any: String
    get() = "any"
  val exists: String
    get() = "exists"
  val `not-exists`: String
    get() = "not-exists"
  val equals: String
    get() = "equals"
  val `does-not-equal`: String
    get() = "does-not-equal"
  val `more-than`: String
    get() = "more-than"
  val `no-more-than`: String
    get() = "no-more-than"
  val `less-than`: String
    get() = "less-than"
  val `no-less-than`: String
    get() = "no-less-than"
  val contains: String
    get() = "contains"
  val `does-not-contain`: String
    get() = "does-not-contain"
  val `starts-with`: String
    get() = "starts-with"
  val `ends-with`: String
    get() = "ends-with"
  val matches: String
    get() = "matches"
  val `does-not-match`: String
    get() = "does-not-match"
  val `ver-more-than`: String
    get() = "ver-more-than"
  val `ver-no-more-than`: String
    get() = "ver-no-more-than"
  val `ver-less-than`: String
    get() = "ver-less-than"
  val `ver-no-less-than`: String
    get() = "ver-no-less-than"
}

fun TCBuildSettings.requirements(builder: TCRequirementsBuilder.() -> Unit) {
  requirements = requirements ?: listOf()

  object : TCRequirementsBuilder {
    override fun rule(id: String?, builder: TCRequirementsBuilderRequirementRef.() -> Unit) {
      object : TCRequirementsBuilderRequirementRef {
        var hasRule = false
        override fun ref(param: String): TCRequirementsBuilderRequirement {
          return object : TCRequirementsBuilderRequirement {
            override fun minus(type: String): TCRequirementsBuilderRequirementValue {
              if (hasRule) throw Error("Only one requirement is allowed in rule block")
              hasRule = true

              val r = TCRequirement().apply {
                this.id = id
                this.type = type
                this.name = param
                requirements = (requirements ?: listOf()) + this
              }

              return object : TCRequirementsBuilderRequirementValue {
                override fun minus(value: String?) {
                  r.value = value
                }
              }
            }
          }
        }
      }.apply { builder() }
    }
  }.builder()
}
