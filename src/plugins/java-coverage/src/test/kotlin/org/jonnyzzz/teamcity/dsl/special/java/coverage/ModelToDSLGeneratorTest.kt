package org.jonnyzzz.teamcity.dsl.special.java.coverage

import org.jonnyzzz.teamcity.dsl.api.coverageEMMA
import org.jonnyzzz.teamcity.dsl.api.coverageIDEA
import org.jonnyzzz.teamcity.dsl.api.coverageJOCOCO
import org.jonnyzzz.teamcity.dsl.api.param
import org.jonnyzzz.teamcity.dsl.context
import org.jonnyzzz.teamcity.dsl.generating.generateRunners
import org.jonnyzzz.teamcity.dsl.generating.kotlinWriter
import org.jonnyzzz.teamcity.dsl.model.TCSettingsRunner
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test


class ModelToDSLGeneratorTest2 {

  @Test
  fun should_use_predefined_runner_mixin2() {
    val r = TCSettingsRunner().apply {
      id = "555"
      runnerType = "zzz"
      coverageIDEA.asBuilder()()
      coverageJOCOCO.asBuilder()()
    }

    val x = kotlinWriter {
      generateRunners(context, listOf(r))(r)
    }.trim()

    println(x)

    Assert.assertTrue(x.trim().startsWith("runner(\"555\", \"zzz\") + coverageIDEA + coverageJOCOCO + {"))
  }

  @Test
  fun should_use_predefined_runner_mixin3() {
    val r = TCSettingsRunner().apply {
      id = "555"
      runnerType = "zzz"
      coverageIDEA.asBuilder()()
      coverageJOCOCO.asBuilder()()
      coverageEMMA.asBuilder()()
    }

    val x = kotlinWriter {
      generateRunners(context, listOf(r))(r)
    }.trim()

    println(x)

    Assert.assertTrue(x.trim().startsWith("runner(\"555\", \"zzz\") + coverageIDEA + coverageEMMA + coverageJOCOCO + {"))
  }

  @Test
  @Ignore
  fun should_use_predefined_runner_mixin_in_mixin() {
    val r1 = TCSettingsRunner().apply {
      id = "555"
      runnerType = "zzz"
      coverageIDEA.asBuilder()()
      coverageJOCOCO.asBuilder()()
      param("z", "x")
      param("q", "x")
    }
    val r2 = TCSettingsRunner().apply {
      id = "777"
      runnerType = "zzz"
      coverageIDEA.asBuilder()()
      coverageJOCOCO.asBuilder()()
      param("z", "x")
      param("R", "x")
    }

    val x = kotlinWriter {
      val d = generateRunners(context, listOf(r1, r2))
      d(r1)
      d(r2)
    }.trim()

    println(x)

    Assert.assertTrue(x.contains("val runnerMixin1 = runnerMixin() + coverageIDEA + coverageJOCOCO + {"))
    Assert.assertTrue(x.contains("runner(\"555\", \"zzz\") + runnerMixin1 + {"))
    Assert.assertTrue(x.contains("runner(\"777\", \"zzz\") + runnerMixin1 + {"))
  }

}