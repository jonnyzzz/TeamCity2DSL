package org.jonnyzzz.teamcity.dsl.special.commandline

import org.jonnyzzz.teamcity.dsl.api.command
import org.jonnyzzz.teamcity.dsl.api.script
import org.jonnyzzz.teamcity.dsl.generating.block
import org.jonnyzzz.teamcity.dsl.generating.quoteWithRefs
import org.jonnyzzz.teamcity.dsl.lang.api.BuildRunnerGenerator
import org.jonnyzzz.teamcity.dsl.lang.api.BuildRunnerGeneratorContext
import org.jonnyzzz.teamcity.dsl.lang.api.BuildRunnerGeneratorResult
import org.jonnyzzz.teamcity.dsl.model.TCSettingsRunner
import org.jonnyzzz.teamcity.dsl.model.find

internal val CMD_runnerType = "simpleRunner"
internal val CMD_use_custom = "use.custom.script"
internal val CMD_content = "script.content"
internal val CMD_command = "command.executable"
internal val CMD_params = "command.parameters"


class CommandlineGenerator : BuildRunnerGenerator(){
  override val priority: Double
    get() = 1001.toDouble()

  override fun generate(context: BuildRunnerGeneratorContext): BuildRunnerGeneratorResult? {
    val runner = context.runner

    if (CMD_runnerType != runner.runnerType) return null

    val custom = runner.parameters?.find(CMD_use_custom)?.value

    if (custom == null) {
      val command = runner.parameters?.find(CMD_command)?.value ?: return null
      val params = runner.parameters?.find(CMD_params)?.value?.split('\n') ?: listOf()

      return BuildRunnerGeneratorResult(setOf(CMD_command, CMD_params)) {
        block("${TCSettingsRunner::command.name}(${command.quoteWithRefs()})") {
          for (p in params) {
            appendln("+ " + p.quoteWithRefs())
          }
        }
      }
    }

    if (custom == "true") {
      val content = runner.parameters?.find(CMD_content)?.value?.split('\n') ?: return null

      return BuildRunnerGeneratorResult(setOf(CMD_use_custom, CMD_content)) {
        block("${TCSettingsRunner::script.name}") {
          for (line in content) {
            appendln("+ " + line.quoteWithRefs())
          }
        }
      }
    }

    return null
  }
}
