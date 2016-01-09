package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.having
import org.jonnyzzz.teamcity.dsl.model.TCSettingsVCSRef
import org.jonnyzzz.teamcity.dsl.model.TCVCSRootRef
import org.jonnyzzz.teamcity.dsl.model.TCWithSettings
import kotlin.collections.any
import kotlin.collections.arrayListOf
import kotlin.collections.listOf
import kotlin.collections.plus


interface VCSRefBuilder {
  operator fun String.unaryPlus() = rule("+:" + this)
  operator fun String.unaryMinus() = rule("-:" + this)
  fun rule(rule : String)
}

fun TCWithSettings.vcs(rootId : TCVCSRootRef, builder : VCSRefBuilder.() -> Unit = {}) {
  settings {
    vcs = (vcs ?: listOf()) + having(TCSettingsVCSRef()) {
      this.rootId = rootId.id!!

      val rules = arrayListOf<String>()
      object : VCSRefBuilder {
        override fun rule(rule: String) {
          rules.add(rule)
        }
      }.builder()
      this.checkoutRule = if (rules.any()) rules else null
    }
  }
}
