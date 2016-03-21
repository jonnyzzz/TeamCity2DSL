package org.jonnyzzz.teamcity.dsl.special.commandline

import org.jonnyzzz.teamcity.dsl.api.command
import org.jonnyzzz.teamcity.dsl.api.script
import org.jonnyzzz.teamcity.dsl.context
import org.jonnyzzz.teamcity.dsl.generating.generateRunners
import org.jonnyzzz.teamcity.dsl.generating.kotlinWriter
import org.jonnyzzz.teamcity.dsl.model.TCSettingsRunner
import org.junit.Assert
import org.junit.Test


class ModelToDSLGeneratorTest2 {

  @Test
  fun should_use_predefined_script() {
    val r = TCSettingsRunner().apply {
      id = "555"
      runnerType = "zzz"
      script("#!/bin/bash\necho \"rules!\"")
    }

    val x = kotlinWriter {
      generateRunners(context, listOf(r))(r)
    }.trim()

    println(x)

    Assert.assertTrue(x.trim().startsWith("runner(\"555\") {\n  script {\n    + \"#!/bin/bash"))
  }

  @Test
  fun should_use_predefined_command() {
    val r = TCSettingsRunner().apply {
      id = "555"
      runnerType = "zzz"
      command("/bin/bash") {
        + "-c"
        + "echo \"hohoho\""
      }
    }

    val x = kotlinWriter {
      generateRunners(context, listOf(r))(r)
    }.trim()

    println(x)

    Assert.assertTrue(x.trim().startsWith("runner(\"555\") {\n  command(\"/bin/bash\") {\n    + \"-c\"\n    + \"echo"))
  }
}