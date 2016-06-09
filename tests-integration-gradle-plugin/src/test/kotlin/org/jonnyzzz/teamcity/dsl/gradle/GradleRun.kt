package org.jonnyzzz.teamcity.dsl.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.jonnyzzz.teamcity.dsl.util.runUnderTempDirectory
import java.io.File

interface RunSetup {
  val home : File
  var script: String

  fun args(vararg s : String)

  fun assert(action : () -> Unit)
}


fun runSuccessfulGradleBuild(setup : RunSetup.() -> Unit): BuildResult {
  return runUnderTempDirectory { temp ->
    val home = temp / "gradle-test"
    home.mkdirs()

    var build_gradle = "No Build"
    val args = mutableListOf("--stacktrace")
    val assertTasks = mutableListOf<() -> Unit>()

    object : RunSetup {
      override var script: String
        get() = build_gradle
        set(value) { build_gradle = value }

      override fun assert(action: () -> Unit) {
        assertTasks.add(action)
      }

      override val home: File
        get() = home

      override fun args(vararg s: String) {
        args.addAll(s)
      }
    }.setup()

    (home / "build.gradle").apply {
      println("Patched project script:\n$build_gradle\n")
      writeText(build_gradle)
    }

    val result = GradleRunner.create()
            .withDebug(true)
            .withProjectDir(home)
            .forwardOutput()
            .withArguments(args.toList())
            .build()

    assertTasks.forEach { it() }
    return@runUnderTempDirectory  result
  }

}
