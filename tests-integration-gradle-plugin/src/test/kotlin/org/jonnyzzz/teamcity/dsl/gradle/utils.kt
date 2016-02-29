package org.jonnyzzz.teamcity.dsl.gradle

import java.io.File

operator fun File.div(s:String) = File(this, s)