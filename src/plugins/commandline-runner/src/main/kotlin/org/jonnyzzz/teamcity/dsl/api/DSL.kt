package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.model.TCSettingsRunner
import org.jonnyzzz.teamcity.dsl.special.commandline.*

interface TCCommandLineScriptBuilder {
  operator fun String.unaryPlus()
}

fun TCSettingsRunner.script(script : String = "", builder : TCCommandLineScriptBuilder.() -> Unit = {}) {
  runnerType = CMD_runnerType

  val text = buildString {
    append(script)

    val sb = this
    object:TCCommandLineScriptBuilder {
      override fun String.unaryPlus() {
        if (sb.length > 0) append("\n")
        append(this)
      }
    }.builder()
  }

  param(CMD_use_custom, "true")
  param(CMD_content, text)
}

interface TCCommandLineCommandBuilder {
  operator fun String.unaryPlus()
  fun arg(a : String) { +a }
}

fun TCSettingsRunner.command(command : String, arguments : TCCommandLineCommandBuilder.() -> Unit = {}) {
  val paramz = mutableListOf<String>()
  object:TCCommandLineCommandBuilder {
    override fun String.unaryPlus() {
      paramz += this
    }
  }.arguments()

  runnerType = CMD_runnerType

  param(CMD_command, command)

  //TODO: may not have the block in runner parameters
  param(CMD_params, paramz.joinToString("\n"))
}
