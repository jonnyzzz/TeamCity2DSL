package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.TCSettingsRunner
import org.jonnyzzz.teamcity.dsl.special.commandline.*


fun TCSettingsRunner.script(script : String) {
  runnerType = CMD_runnerType

  param(CMD_use_custom, "true")
  param(CMD_content, script)
}

interface TCCommandLineCommandBuilder {
  operator fun String.unaryPlus()
  fun arg(a : String) { +a }
}

fun TCSettingsRunner.command(command : String, arguments : TCCommandLineCommandBuilder.() -> Unit) {
  val paramz = mutableListOf<String>()
  object:TCCommandLineCommandBuilder {
    override fun String.unaryPlus() {
      paramz += this
    }
  }.arguments()

  runnerType = CMD_runnerType

  param(CMD_command, command)
  param(CMD_params, paramz.joinToString("\n"))
}
