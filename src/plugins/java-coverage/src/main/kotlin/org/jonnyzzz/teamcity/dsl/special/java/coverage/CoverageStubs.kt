package org.jonnyzzz.teamcity.dsl.special.java.coverage

import org.jonnyzzz.teamcity.dsl.api.coverageEMMA
import org.jonnyzzz.teamcity.dsl.api.coverageIDEA
import org.jonnyzzz.teamcity.dsl.api.coverageJOCOCO
import org.jonnyzzz.teamcity.dsl.lang.api.BuildRunnerExtensionGenerator
import org.jonnyzzz.teamcity.dsl.lang.api.BuildRunnerExtensionGeneratorContext
import org.jonnyzzz.teamcity.dsl.lang.api.BuildRunnerExtensionGeneratorResult
import org.jonnyzzz.teamcity.dsl.model.find


val coverageJOCOCO_param = "teamcity.coverage.jacoco.patterns"
val coverageJOCOCO_value = "+:*"


val coverageIDEA_param = "teamcity.coverage.idea.includePatterns"
val coverageIDEA_value = "*"

val coverageEMMA_include_param = "teamcity.coverage.emma.include.source"
val coverageEMMA_include_value = "true"
val coverageEMMA_instr_param = "teamcity.coverage.emma.instr.parameters"
val coverageEMMA_instr_value = "-ix -*Test*"


class JOCOCOCoverageRunnerExtension : BuildRunnerExtensionGenerator() {
  override val priority: Double = 1003.toDouble()

  override fun generate(context: BuildRunnerExtensionGeneratorContext): BuildRunnerExtensionGeneratorResult? {
    val runner = context.runner

    val jococo = runner.parameters.find(coverageJOCOCO_param) ?: return null
    if (jococo.value != coverageJOCOCO_value) return null

    return BuildRunnerExtensionGeneratorResult(setOf(coverageJOCOCO_param)) {
      constant(::coverageJOCOCO.name)
    }
  }
}

class IDEACoverageRunnerExtension : BuildRunnerExtensionGenerator() {
  override val priority: Double = 1001.toDouble()

  override fun generate(context: BuildRunnerExtensionGeneratorContext): BuildRunnerExtensionGeneratorResult? {
    val runner = context.runner

    val idea = runner.parameters.find(coverageIDEA_param) ?: return null
    if (idea.value != coverageIDEA_value) return null

    return BuildRunnerExtensionGeneratorResult(setOf(coverageIDEA_param)) {
      constant(::coverageIDEA.name)
    }
  }
}

class EMMACoverageRunnerExtension : BuildRunnerExtensionGenerator() {
  override val priority: Double = 1002.toDouble()

  override fun generate(context: BuildRunnerExtensionGeneratorContext): BuildRunnerExtensionGeneratorResult? {
    val runner = context.runner

    val emma_include = runner.parameters.find(coverageEMMA_include_param) ?: return null
    val emma_instr = runner.parameters.find(coverageEMMA_instr_param) ?: return null

    if (emma_include.value != coverageEMMA_include_value) return null
    if (emma_instr.value != coverageEMMA_instr_value) return null

    return BuildRunnerExtensionGeneratorResult(setOf(coverageEMMA_include_param, coverageEMMA_instr_param)) {
      constant(::coverageEMMA.name)
    }
  }
}

