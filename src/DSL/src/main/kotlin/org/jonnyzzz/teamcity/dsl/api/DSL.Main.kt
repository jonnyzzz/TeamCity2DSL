package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.using
import java.io.File
import java.io.InputStreamReader
import java.net.URLClassLoader

interface TCDSLLazy {
  fun doLazyInit()
}

/// Main method for generator
fun main(args: Array<String>) {
  val code = teamcity_dls_generator_main(*args)
  System.exit(code)
}

object DSLMain {
  @JvmStatic
  fun main(args: Array<String>) {
    val code = teamcity_dls_generator_main(*args)
    System.exit(code)
  }
}

/// This is the prototype for the main method
/// of the generator
fun teamcity_dls_generator_main(vararg args: String) : Int {
  try {
    return teamcity_dls_generator_main_impl(*args)
  } catch (t : Throwable) {
    println("Unknown error: ${t.message}")
    t.printStackTrace(System.out)
    return 2
  }
}

val version : String by lazy(LazyThreadSafetyMode.NONE) {
  try {
    class M
    using(M::class.java.getResourceAsStream("/BUILD")!!) {
      com.google.common.io.CharStreams.toString(InputStreamReader(this, "utf-8"))?.trim()!!
    }
  } catch(t: Throwable) {
    "SNAPSHOT"
  }
}

fun teamcity_dls_generator_main_impl(vararg args: String) : Int {
  println("TeamCity.DSL project files generator")
  println("  version: $version")
  println("  arguments: ${args.joinToString(" ")}")

  fun help() {
    val app = "java -jar TeamCityDSL.jar"

    println()
    println()
    println("Usage:")
    println("  $app generate <classes package> <destination dir> [<classpath element>*]")
    println("     application assumes compiled DSL classes are in the application's classpath")
    println("     <classes package>   - root package name of DSL classes")
    println("     <destination dir>   - destination directory for generated")
    println("                           TeamCity project files. WILL BE CLEANED")
    println("     <classpath element> - .jar file or directory of compiled DSL classes")
    println("                           uses start classpath by default")
    println()
    println("  $app import <classes package> <project .xml files> <destination dir>")
    println("     generates DSL code from existing TeamCity configuration files")
    println("     <classes package>    - root package name for generated classes")
    println("     <project .xml files> - directory with TeamCity original .xml ")
    println("                            project files")
    println("     <destination dir>    - destination directory for generated")
    println("                            DSL files. WILL BE CLEANED")
    println()
    println("  $app xml <classes package> <project .xml files> <destination dir>")
    println("     loads and saves back .xml files model. Allows to test generator")
    println("     <project .xml files> - directory with TeamCity original .xml ")
    println("                            project files")
    println("     <destination dir>    - destination directory for generated")
    println("                            TeamCity project files. WILL BE CLEANED")
    println()
    println()
    println()
  }

  if (args.size < 1)  {
    println("Invalid arguments")
    help()
    return 1
  }

  val mode = args[0]
  if (mode == "generate") {
    if (args.size < 3) {
      println("Invalid arguments")
      help()
      return 1
    }

    val pkg = args[1]
    val file = File(args[2]).canonicalFile

    println("From package:    $pkg")
    println("Generating into: $file...")

    val classpath = args.asSequence().drop(3).toList()
    if (classpath.isEmpty()) {
      org.jonnyzzz.teamcity.dsl.api.internal.generateProjects(file, pkg)
    } else {
      println("Loading classes from: " + classpath)
      class M
      val cl = URLClassLoader(classpath.map{File(it).toURI().toURL()}.toTypedArray(), M::class.java.classLoader)
      org.jonnyzzz.teamcity.dsl.api.internal.generateProjects(file, pkg, cl)
    }

    return 0
  }

  if (mode == "import") {
    if (args.size < 4)  {
      println("Invalid arguments")
      help()
      return 1
    }

    val pkg = args[1]
    val model = File(args[2]).canonicalFile
    val dest = File(args[3]).canonicalFile
    println("Importing model from: $model")
    println("                into: $dest")
    println("             package: $pkg")
    org.jonnyzzz.teamcity.dsl.api.internal.importProjects(model, pkg, dest)
    return 0
  }

  if (mode == "xml") {
    if (args.size < 3)  {
      println("Invalid arguments")
      help()
      return 1
    }
    val model = File(args[1]).canonicalFile
    val dest = File(args[2]).canonicalFile
    println("Load-Save XML model from: $model")
    println("                    into: $dest")
    org.jonnyzzz.teamcity.dsl.api.internal.loadSave(model, dest)
    return 0
  }

  if (mode == "help") {
    help()
    return 0
  }

  println("Unknown parameters.")
  return 1
}
