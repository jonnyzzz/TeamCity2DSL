package org.jonnyzzz.teamcity.dsl.generating

interface KotlinWriter {
  fun appendln(line : String = "")

  fun offset() : KotlinWriter
}

interface KotlinMixinkWriter {
  fun constant(name : String)
  fun function(name : String, vararg params : String)
  fun block(name : String, vararg params : String, blockWriter: KotlinWriter.() -> Unit = { })
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
