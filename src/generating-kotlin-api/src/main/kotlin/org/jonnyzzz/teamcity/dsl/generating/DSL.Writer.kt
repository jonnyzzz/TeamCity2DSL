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
