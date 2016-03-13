package org.jonnyzzz.teamcity.dsl.util

import org.jonnyzzz.teamcity.dsl.main.teamcity_dls_generator_main
import org.jonnyzzz.teamcity.dsl.xml.XmlGenerating
import org.jonnyzzz.teamcity.dsl.xml.XmlParsing
import org.junit.Assert
import org.junit.Test
import org.junit.experimental.categories.Category
import java.io.File

interface GenerateDSLCategory
interface GenerateXMLCategory
interface GenerateCheckCategory


internal object ProvidedPaths {
  val DSL_package = "org.jonnyzzz.autodsl.integration.generated"
  val DSL_classes by lazyPropertyPath("DATA_DSL_CLASSES")
  val DSL_out by lazyPropertyPath("DATA_DSL_OUT")

  val XMLs_in by lazyPropertyPath("DATA_XMLS_IN")
  val XMLs_out by lazyPropertyPath("DATA_XMLS_OUT")

  private fun lazyPropertyPath(property : String) = lazy {
    val path = System.getProperty(property)
    Assert.assertNotNull("property $property is not defined", path)
    val file = File(path)
    return@lazy file.absoluteFile
  }
}

@Category(GenerateDSLCategory::class)
abstract class GenerateDSLsTestBase {
  @Test
  fun `load save XML is ID`() {
    val model = XmlParsing.parse(ProvidedPaths.XMLs_in)

    runUnderTempDirectory { temp ->
      XmlGenerating.generate(model, temp)

      assertGeneratedTeamCityModel(ProvidedPaths.XMLs_in, temp)
    }
  }

  @Test
  fun `generate DSL from XML`() {
    teamcity_dls_generator_main("import",
            ProvidedPaths.DSL_package,
            ProvidedPaths.XMLs_in.absolutePath,
            ProvidedPaths.DSL_out.absolutePath)
  }
}

@Category(GenerateXMLCategory::class)
abstract class GenerateXMLsTestBase {
  @Test
  fun `generate XML from DSL`() {
    teamcity_dls_generator_main("generate",
            ProvidedPaths.DSL_package,
            ProvidedPaths.XMLs_out.absolutePath,
            ProvidedPaths.DSL_classes.absolutePath)
  }
}

@Category(GenerateCheckCategory::class)
abstract class IntegrationTestBase() {
  @Test
  fun `check generated XML is same as test data`() {
    assertGeneratedTeamCityModel(ProvidedPaths.XMLs_in, ProvidedPaths.XMLs_out)
  }
}

