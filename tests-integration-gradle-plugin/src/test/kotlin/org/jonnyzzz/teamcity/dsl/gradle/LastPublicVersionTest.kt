package org.jonnyzzz.teamcity.dsl.gradle

import org.junit.Test

class LastPublicVersionTest {
  @Test
  fun testVersion() {
    println(TeamCity2DSLPlugin.DSL_PLUGIN_LATEST_PUBLIC_VERSION)
  }
}
