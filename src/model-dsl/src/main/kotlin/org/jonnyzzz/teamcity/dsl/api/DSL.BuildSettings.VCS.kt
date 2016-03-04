package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.TCSettingsVCSRef
import org.jonnyzzz.teamcity.dsl.model.TCVCSRootRef
import org.jonnyzzz.teamcity.dsl.model.TCWithSettings


interface VCSRefBuilder {
  operator fun String.unaryPlus() = rule("+:" + this)
  operator fun String.unaryMinus() = rule("-:" + this)
  fun rule(rule : String)
}


interface TCCheckoutBuilder {
  fun vcs(rootId: TCVCSRootRef, builder: VCSRefBuilder.() -> Unit = {})
}

fun TCWithSettings.checkout(builder : TCCheckoutBuilder.() -> Unit) {
  settings {
    vcs = vcs ?: listOf()
  }

  object : TCCheckoutBuilder {
    override fun vcs(rootId : TCVCSRootRef, builder : VCSRefBuilder.() -> Unit) {
      settings {
        vcs = (vcs ?: listOf()) + TCSettingsVCSRef().apply {
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

  }.builder()
}
