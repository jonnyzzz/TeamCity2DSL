package org.jonnyzzz.teamcity.dsl.gradle

object TeamCity2DSLPlugin {
  val DSL_PLUGIN_CLASSPATH by lazy { System.getProperty("TEST_PLUGIN_CLASSPATH")!! }
  val DSL_PLUGIN_VERSION by lazy { System.getProperty("TEST_PLUGIN_VERSION")!! }
  val DSL_PLUGIN_NAME by lazy { System.getProperty("TEST_PLUGIN_NAME")!! }
}

