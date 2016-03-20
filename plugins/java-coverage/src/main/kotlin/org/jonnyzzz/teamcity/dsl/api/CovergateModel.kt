package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.special.java.coverage.*

val coverageJOCOCO: TCRunnerMixin = runnerMixin {
  param(coverageJOCOCO_param, coverageJOCOCO_value)
}

val   coverageIDEA: TCRunnerMixin = runnerMixin {
  param(coverageIDEA_param, coverageIDEA_value)
}

val coverageEMMA: TCRunnerMixin = runnerMixin {
  param(coverageEMMA_include_param, coverageEMMA_include_value)
  param(coverageEMMA_instr_param, coverageEMMA_instr_value)
}
