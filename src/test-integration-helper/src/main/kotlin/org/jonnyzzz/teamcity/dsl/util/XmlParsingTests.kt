package org.jonnyzzz.teamcity.dsl.util

import org.jonnyzzz.teamcity.dsl.xml.XmlGenerating
import org.jonnyzzz.teamcity.dsl.xml.XmlParsing
import org.junit.Assert
import org.junit.Test
import java.io.File


open class IntegrationTestBase() {
  val XMLs_in by lazy {
    File(System.getProperty("DATA_XMLS_IN")).apply {
      Assert.assertTrue("$this should exist", this.isDirectory)
    }
  }

  val XMLs_out by lazy {
    File(System.getProperty("DATA_XMLS_OUT")).apply {
      Assert.assertTrue("$this should exist", this.isDirectory)
    }
  }

  @Test
  fun test_assert_generated_xml_is_same() {
    assertGeneratedTeamCityModel(XMLs_in, XMLs_out)
  }

  @Test
  fun test_load_safe_xml() {
    val model = XmlParsing.parse(XMLs_in)

    runUnderTempDirectory { temp ->
      XmlGenerating.generate(model, temp)

      assertGeneratedTeamCityModel(XMLs_in, temp)
    }
  }
}

