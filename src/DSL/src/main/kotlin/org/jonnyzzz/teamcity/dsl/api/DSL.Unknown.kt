package org.jonnyzzz.teamcity.dsl.api

import org.jdom2.Content
import org.jdom2.Element
import org.jdom2.Text

interface TCUnknownBuilder {
  fun element(name : String, builder : TCUnknownBuilder.() -> Unit = {})
  fun attribute(name : String, value : String)
  fun text(text : String)
}

fun elementImpl(name : String, builder : TCUnknownBuilder.() -> Unit) : Element {
  val element = Element(name)
  object : TCUnknownBuilder {
    override fun element(name: String, builder: TCUnknownBuilder.() -> Unit) {
      element.addContent(elementImpl(name,builder) as Content)
    }

    override fun attribute(name: String, value: String) {
      element.setAttribute(name, value)
    }

    override fun text(text: String) {
      element.addContent(Text(text))
    }
  }.builder()
  return element
}
