package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.having
import org.jonnyzzz.teamcity.dsl.model.TCProjectRef
import org.jonnyzzz.teamcity.dsl.model.TCVCSRoot
import org.jonnyzzz.teamcity.dsl.model.TCVCSRootRef
import kotlin.collections.forEach
import kotlin.collections.listOf
import kotlin.collections.plus

val gitRoot : TCVCSRootMixin = vcsRootMixin {
  vcsType = "jetbrains.git"

  param("agentCleanFilesPolicy", "ALL_UNTRACKED")
  param("agentCleanPolicy", "ON_BRANCH_CHANGE")
  param("ignoreKnownHosts", "true")
  param("submoduleCheckout", "CHECKOUT")
  param("usernameStyle", "USERID")
}

interface TCVCSRootMixin {
  fun asBuilder() : TCVCSRoot.() -> Unit
}

interface TCVCSRootMixinBuilder : TCVCSRootMixin {
  operator fun plus(mixin : TCVCSRootMixin) : TCVCSRootMixinBuilder = this + mixin.asBuilder()
  operator fun plus(builder : TCVCSRoot.() -> Unit) : TCVCSRootMixinBuilder
}

interface TCVCSRootBuilder : TCVCSRootRef {
  operator fun plus(mixin : TCVCSRootMixin) : TCVCSRootBuilder = this + mixin.asBuilder()
  operator fun plus(builder : TCVCSRoot.() -> Unit) : TCVCSRootBuilder
}

fun vcsRootMixin(builder : TCVCSRoot.() -> Unit = {}) : TCVCSRootMixinBuilder {
  class VCSRootMixinBuilderImpl(val actions : List<TCVCSRoot.() -> Unit> = listOf()) : TCVCSRootMixinBuilder {
    override fun asBuilder(): TCVCSRoot.() -> Unit = { actions.forEach { it() } }
    override fun plus(builder: TCVCSRoot.() -> Unit) = VCSRootMixinBuilderImpl(actions + builder)
  }
  return VCSRootMixinBuilderImpl() + builder
}

fun TCProjectRef.vcsRoot(id : String, builder : TCVCSRoot.() -> Unit = {}) : TCVCSRootBuilder {
  val builders = LazyBuilders(builder)
  val result = object : TCVCSRoot(), TCDSLLazy by builders {
    init {
      this.id = id
    }
  }
  builders.instance = result
  if (this is TCProjectRefOnReady) this.onReady { this.vcsRoots += result }

  return object: TCVCSRootBuilder, TCVCSRootRef by result  {
    operator override fun plus(builder: TCVCSRoot.() -> Unit): TCVCSRootBuilder = having(this) { builders.add(builder) }
  }
}

fun UnknownVCSRoot(id: String): TCVCSRootRef = having(TCVCSRoot()) { this.id = id }