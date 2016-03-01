package org.jonnyzzz.teamcity.dsl.generating

import org.apache.commons.lang3.StringEscapeUtils
import org.jdom2.Element
import org.jonnyzzz.teamcity.dsl.api.version
import java.util.*
import kotlin.collections.forEach
import kotlin.text.isEmpty
import kotlin.text.replace

fun generateKotlinDSL(pkg: String, jvmFileName : String, builder : KotlinWriter.() -> Unit) : String {
  return kotlinWriter {
    generateKotlinDSLFileHeader(pkg, jvmFileName)

    builder()
    appendln()
  }
}

fun KotlinWriter.generateKotlinDSLFileHeader(pkg: String, jvmFileName : String)  {
  appendln("///////////////////////////////////////////")
  appendln("/// THIS IS AUTO GENERATED FILE")
  appendln("/// YOU MAY EDIT IT ON YOUR OWN RISK")
  appendln("/// TeamCity.DSL VERSION=${version}")
  appendln("///////////////////////////////////////////")
  appendln("@file:JvmName(\"jonnyzzz_${jvmFileName.replace(Regex("[^a-zA-Z0-9\\-]"), "_")}\")   ")
  appendln("@file:Suppress(\"PackageDirectoryMismatch\")")
  appendln()
  appendln("package $pkg")
  appendln()
  appendln("import org.jonnyzzz.teamcity.dsl.api.*")
  appendln()
  appendln()
}

fun KotlinWriter.generateTCDSL(className : String, action : KotlinWriter.() -> Unit) {
  block2("object $className : TCDSL()") {
    action()
  }
}

fun generateKotlinDSL(pkg:String, jvmFileName : String, className : String, action : KotlinWriter.() -> Unit) : String {
  return generateKotlinDSL(pkg, jvmFileName) {
    generateTCDSL(className){
      action()
    }
  }
}

fun KotlinWriter.project(id : String, uuid : String?, builder : KotlinWriter.() -> Unit) {
  when(uuid) {
    null -> block("project(${id.quote()})") {
      builder()
    }
    else -> block("project(${id.quote()}, ${uuid.quote()})") {
      builder()
    }
  }
}

fun KotlinWriter.build(projectId : String, buildId : String, uuid : String?, builder : KotlinWriter.() -> Unit) {
  project(projectId, null) {
    when(uuid) {
      null -> block("build(${buildId.quote()})") {
        builder()
      }
      else -> block("build(${buildId.quote()}, ${uuid.quote()})") {
        builder()
      }
    }
  }
}

fun KotlinWriter.template(projectId : String, templateId : String, uuid : String?, builder : KotlinWriter.() -> Unit) {
  project(projectId, null) {
    when(uuid) {
      null -> block("template(${templateId.quote()})") {
        builder()
      }
      else -> block("template(${templateId.quote()}, ${uuid.quote()})") {
        builder()
      }
    }
  }
}

fun KotlinWriter.element(element : Element?, assignment : String = "") {
  if (element == null) return
  block("${assignment}element(${element.getName()?.quote()})") {
    elementInternals(element)
  }
}

fun KotlinWriter.elementInternals(element : Element?) {
  if (element == null) return

  element.getAttributes()?.forEach { appendln("attribute(${it.getName()?.quote()}, ${it.getValue()?.quote()})") }
  element.getChildren()?.forEach { element(it) }
  val text = element.getTextTrim() ?: ""
  if (!text.isEmpty()) {
    appendln("text(${text.quote()})")
  }
}

fun KotlinWriter.block(text : String, omitEmpty : Boolean = false, builder : KotlinWriter.() -> Unit) {
  var hasContent = false
  val that = this
  val offset = that.offset()
  object : KotlinWriter by offset {
    override fun appendln(line: String) {
      if (hasContent == false) {
        that.appendln(text + " {")
        hasContent = true
      }
      offset.appendln(line)
    }
  }.builder()

  when {
    hasContent -> appendln("}")
    !omitEmpty -> appendln(text + " { }")
    else -> appendln(text)
  }
}

fun KotlinWriter.block2(text : String, builder : KotlinWriter.() -> Unit) {
  appendln(text + " {init{")
  offset().builder()
  appendln("}}")
}

fun KotlinWriter.setter(name : String, value : String?) {
  if (value == null) return
  val encoded = value.quote()
  appendln("$name = $encoded")
}

fun KotlinWriter.setter(name : String, value : Element?) {
  if (value == null) return
  element(value, "$name = ")
}

fun String.quote() : String {
  val encoded = StringEscapeUtils.escapeJava(this)!!.replace("\$", "\\\$")
  return "\"$encoded\""
}

