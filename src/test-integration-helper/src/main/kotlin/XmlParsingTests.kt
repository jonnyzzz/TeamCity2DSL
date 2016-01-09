import org.jonnyzzz.teamcity.dsl.deleteAll
import org.jonnyzzz.teamcity.dsl.div
import org.jonnyzzz.teamcity.dsl.generating.DSLGenerating
import org.jonnyzzz.teamcity.dsl.generating.DSLOptions
import org.jonnyzzz.teamcity.dsl.having
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.util.assertGeneratedTeamCityModel
import org.jonnyzzz.teamcity.dsl.util.dumpFiles
import org.jonnyzzz.teamcity.dsl.util.runUnderTempDirectory
import org.jonnyzzz.teamcity.dsl.writeUTF
import org.jonnyzzz.teamcity.dsl.xml.XmlGenerating
import org.jonnyzzz.teamcity.dsl.xml.XmlParsing
import org.junit.Assert
import org.junit.Test
import java.io.File
import kotlin.collections.map
import kotlin.text.replace
import kotlin.text.toRegex


abstract class XmlParsingTestCase(val root: String) {

  val testDataPath: File by lazy(LazyThreadSafetyMode.NONE) {
    File("./testData").canonicalFile / root
  }

  protected fun loadProject(): List<TCProject> = XmlParsing.parse(testDataPath)

  @Test
  fun test_generate_kotlin_DSL() {

    /// It is quite complicated to have Kotlin compile
    /// call from the tests
    ///
    /// we use another approach:
    ///  1) this tests generates Kotlin code to the DSL.Samples project
    ///  2) marker files are included to simplify further tests
    ///  3) Tests from DSL.Samples are used to ensure generated classes are back-mappable to XML

    val orig = testDataPath
    val model = XmlParsing.parse(orig)

    val dest = File("./DSL.Samples/tests-gen").canonicalFile / orig.name.replace("[^a-zA-Z0-9]+".toRegex(), "_")
    dest.deleteAll()
    dest.mkdirs()

    var options = having(DSLOptions()) {
      packageName = "org.jonnyzzz.autodsl.tests.${orig.name.replace("[^a-zA-Z0-9]+".toRegex(), "_")}"
    }

    DSLGenerating.generate(model, dest, options)
    dest.dumpFiles()

    (dest / "gold.ref").writeUTF { testDataPath.absolutePath }
    (dest / "gold.package").writeUTF { options.packageName }
  }

  @Test
  fun test_load_safe_xml() {
    val orig = testDataPath
    val model = XmlParsing.parse(orig)

    runUnderTempDirectory { temp ->
      XmlGenerating.generate(model, temp)

      assertGeneratedTeamCityModel(orig, temp)
    }
  }

  @Test
  fun projects_must_be_detected() {
    val ps = loadProject().map { it.id }

    Assert.assertTrue("Empty projects set!", ps.size > 0)
  }
}

