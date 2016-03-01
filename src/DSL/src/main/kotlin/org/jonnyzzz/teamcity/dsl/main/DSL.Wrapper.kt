package org.jonnyzzz.teamcity.dsl.main

import java.io.File

object DSLRunner {
  @JvmStatic
  fun importProjects(xmlRoot : File, pkg : String, destRoot : File) : Unit {
    org.jonnyzzz.teamcity.dsl.main.importProjects(xmlRoot, pkg, destRoot)
  }
}

