package org.jonnyzzz.teamcity.dsl.xml

import org.jdom2.DocType
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import org.jdom2.output.support.AbstractXMLOutputProcessor
import org.jdom2.output.support.FormatStack
import org.jdom2.util.NamespaceStack
import java.io.Writer

/**
 * Created by eugene.petrenko@gmail.com
 */

fun xmlOutputter() : XMLOutputter {
  val format = Format.getPrettyFormat()!!;
  format.setLineSeparator(System.getProperty("teamcity.dsl.lineSeparator.xml", "\n"));
  format.encoding = "UTF-8";

  val out = XMLOutputter(format)
  out.xmlOutputProcessor = object: AbstractXMLOutputProcessor() {
    override fun printDocType(out: Writer?, fstack: FormatStack?, docType: DocType?) {
      super.printDocType(out, fstack, docType)

      write(out, fstack!!.lineSeparator)
    }

    override fun printDocument(out: Writer?, fstack: FormatStack?, nstack: NamespaceStack?, doc: Document?) {
      super.printDocument(out, fstack, nstack, doc)

      write(out, fstack!!.lineSeparator)
    }
  }
  return out
}


fun generateXMLString(root : Element) : String {
  return xmlOutputter().outputElementContentString(root)
}

fun Element.toDocument(postProcess: Document.() -> Unit = {}): Document = let { it ->
  Document().apply {
    rootElement = it
    postProcess()
  }
}
