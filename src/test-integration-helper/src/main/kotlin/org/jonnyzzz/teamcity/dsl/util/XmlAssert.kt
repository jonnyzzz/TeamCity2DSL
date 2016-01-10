package org.jonnyzzz.teamcity.dsl.util


import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.loadUTF
import java.io.File

fun assertGeneratedTeamCityModel(orig : File, temp : File) {
  fun File.loadForCompare() = loadUTF().replace("\r\n", "\n")

  fun selectFilesToCompare(root: File) : Map<String, String> = root
          .listFiles { it -> it.isDirectory() }!!
          .flatMap {
            val key = "project:" + it.name
            val projectConfig = it / "project-config.xml"
            if (!projectConfig.isFile())
              listOf()
            else
              listOf(key to projectConfig.loadForCompare()) +
                    ((it / "buildTypes")
                            .listFiles { it -> it.isFile() && it.name.endsWith(".xml") }
                            ?.map { (key + "/Build_" + it.name) to it.loadForCompare() }
                            ?: listOf()) +
                    ((it / "vcsRoots")
                            .listFiles { it -> it.isFile() && it.name.endsWith(".xml") }
                            ?.map { (key + "/VCSRoot_" + it.name) to it.loadForCompare() }
                            ?: listOf()) +
                    ((it / "pluginData")
                            .listFiles { it -> it.isFile() && it.name.endsWith(".xml") }
                            ?.map { (key + "/" + it.name) to it.loadForCompare() }
                            ?: listOf()) +
                    ((it / "pluginData" / "metaRunners")
                            .listFiles { it -> it.isFile() && it.name.endsWith(".xml") }
                            ?.map { (key + "/meta-" + it.name) to it.loadForCompare() }
                            ?: listOf())
          }.toMap()

  val origData = selectFilesToCompare(orig)
  val actualData = selectFilesToCompare(temp)

  println()
  println("Expected files:\n" + origData.keys.joinToString("\n"))
  println()
  println("Actual files:\n" + actualData.keys.joinToString("\n"))

  println()
  val errors = origData.filter { kv ->
    val (file, origText) = kv
    actualData[file] != origText
  } . map { it.key } .toSortedSet()
  println("Total errors: ${errors.size} of ${origData.size}: ${errors}")
  println()

  for ((file, origText) in origData) {
    println("Checking: " + file)

    val actualText = actualData[file]
    org.junit.Assert.assertNotNull("File $file should be genereated", actualText)
    try {
      org.junit.Assert.assertEquals("$file is generated incorrectly}", origText, actualText)
    } catch (t: Throwable) {
      println("Generated file:")
      println(actualText)
      println("================")
      println("Expected file:")
      println(origText)
      throw t
    }
  }

  org.junit.Assert.assertEquals(origData, actualData)
}

