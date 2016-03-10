package org.jonnyzzz.teamcity.dsl.xml

import org.jdom2.Element
import org.jonnyzzz.kotlin.xml.bind.jdom.JDOM


fun <T : Any> T.bind(rootElement: Element) : T = apply { JDOM.bind(rootElement, this) }
