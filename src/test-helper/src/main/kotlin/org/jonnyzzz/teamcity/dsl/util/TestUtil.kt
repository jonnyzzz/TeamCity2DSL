package org.jonnyzzz.teamcity.dsl.util

import com.google.common.io.Files
import org.jonnyzzz.teamcity.dsl.deleteAll
import org.jonnyzzz.teamcity.dsl.loadUTF
import java.io.File

inline fun <Y> runUnderTempDirectory(action: (File) -> Y) : Y {
  val file = Files.createTempDir()!!.canonicalFile;
  file.mkdirs();
  try {
    return action(file);
  } finally {
    file.deleteAll()
  }
}


fun File.dumpFiles(parentName : String = "") {
  if (isFile) {
    println("-------------")
    println(parentName + name)
    println(loadUTF())
    println()
  } else {
    val prefix = parentName + name + "/"
    listFiles{ it -> it.isDirectory }?.sorted()?.forEach { it.dumpFiles(prefix) }
    listFiles{ it -> it.isFile }?.sorted()?.forEach { it.dumpFiles(prefix) }
  }
}
