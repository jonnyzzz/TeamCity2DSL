package org.jonnyzzz.teamcity.dsl

import org.jonnyzzz.teamcity.dsl.api.*
import org.jonnyzzz.teamcity.dsl.generating.*
import org.jonnyzzz.teamcity.dsl.model.*
import org.jonnyzzz.teamcity.dsl.util.dumpFiles
import org.jonnyzzz.teamcity.dsl.util.runUnderTempDirectory
import org.junit.Assert
import org.junit.Test

class ModelToDSLGeneratorTest {

  @Test
  fun should_generate_no_extra_spaces_in_ordering_1() {
    val p = object:TCProject("q"){ }.apply {
      ordering {
        + UnknownBuild("p1")
        + UnknownBuild("p2")
      }
    }

    val x = kotlinWriter {
      generateOrdering(contextWithLookup, p)
    }.trim()


    Assert.assertEquals("ordering {\n  + Build_p1.id\n  + Build_p2.id\n}", x)
  }

  @Test
  fun should_generate_no_extra_spaces_in_ordering_2() {
    val p = object:TCProject("q"){ }.apply {
      ordering {
        + UnknownProject("p1")
        + UnknownProject("p2")
      }
    }

    val x = kotlinWriter {
      generateOrdering(contextWithLookup, p)
    }.trim()


    Assert.assertEquals("ordering {\n  + Project_p1.id\n  + Project_p2.id\n}", x)
  }

  @Test
  fun should_generate_no_extra_spaces_in_ordering_3() {
    val p = object:TCProject("q"){ }.apply {
      ordering { }
    }

    val x = kotlinWriter {
      generateOrdering(contextWithLookup, p)
    }.trim()


    Assert.assertEquals("", x)
  }

  @Test
  fun should_generate_no_extra_spaces_in_ordering_4() {
    val p = object:TCProject("q"){ }.apply {
      ordering {
        + UnknownProject("p1")
        + UnknownProject("p2")
        + UnknownBuild("b1")
        + UnknownBuild("b2")
      }
    }

    val x = kotlinWriter {
      generateOrdering(contextWithLookup, p)
    }.trim()


    Assert.assertEquals("ordering {\n  + Project_p1.id\n  + Project_p2.id\n\n  + Build_b1.id\n  + Build_b2.id\n}", x)
  }

  @Test
  fun should_generate_for_simple_project() {
    val p = object:TCProject("p"){ }.apply {
      name = "foo"
      description = "bar"
      parameters = arrayListOf(
              TCParameterWithSpec().apply { name = "aaa"; value = "bbb" },
              TCParameterWithSpec().apply { name = "ccc"; value = "ddd" })
    }
    runUnderTempDirectory { tmp ->
      DSLGenerating.generate(TeamCityModel(TeamCityVersion.v9, arrayListOf(p)), tmp)

      tmp.dumpFiles()
    }
  }


  @Test
  fun should_replace_references_01() {

    Assert.assertEquals("\"\${ref(\"foo\")} \"", "%foo% ".quoteWithRefs())
    Assert.assertEquals("\"\${ref(\"foo\")} \${ref(\"bar\")}\"", "%foo% %bar%".quoteWithRefs())
    Assert.assertEquals("\"this \${ref(\"foo\")}\"", "this %foo%".quoteWithRefs())
    Assert.assertEquals("\"%%\"", "%%".quoteWithRefs())
    Assert.assertEquals("\"%%%\"", "%%%".quoteWithRefs())
    Assert.assertEquals("\"%%%   \"", "%%%   ".quoteWithRefs())
    Assert.assertEquals("\"%aaa\"", "%aaa".quoteWithRefs())

  }

  @Test
  fun should_avoid_quites_if_simple() {
    Assert.assertEquals("ref(\"foo\")", "%foo%".quoteWithRefs())
  }

  @Test
  fun should_use_constant_for_requirements() {

    val r = TCRequirement().apply {
      name = "aaa"
      value = "bbb"
      type = "equals"
    }

    val x = kotlinWriter {
      generateRequirement(r)
    }.trim()

    Assert.assertEquals("rule {\n  ref(\"aaa\") - `equals` - \"bbb\"\n}", x)
  }

  @Test
  fun should_use_constant_for_requirements_id() {

    val r = TCRequirement().apply {
      id = "bb"
      name = "aaa"
      value = "bbb"
      type = "equals"
    }

    val x = kotlinWriter {
      generateRequirement(r)
    }.trim()

    Assert.assertEquals("rule(\"bb\") {\n  ref(\"aaa\") - `equals` - \"bbb\"\n}", x)
  }


  @Test
  fun should_generate_nice_options() {
    val input = TCSettingsOptions().apply {
        option("artifactRules", "worker/worker-server/logs => %logs%.zip")
        option("buildNumberPattern", "${ref("dep.VCS_GitHostingTrunk_BuildNumber.build.number")}-${ref("build.counter")}")
        option("executionTimeoutMin", "60")
        option("maximumNumberOfBuilds", "60")
    }

    val x = kotlinWriter {
      generateSettingsOptions(input)
    }

    println(x)

    Assert.assertFalse(x.contains("option("))
    Assert.assertFalse(x.contains("%"))
  }

  @Test
  fun should_generate_nice_options_artifact_newlines() {
    val input = TCSettingsOptions().apply {
        option("artifactRules", "\nworker/worker-server/logs => %logs%.zip\n")
    }

    val x = kotlinWriter {
      generateSettingsOptions(input)
    }

    println(x)
    Assert.assertTrue("should be 3++", x.filter { it == '+' }.count() == 3)
  }

  @Test
  fun should_generate_nice_param_newlines() {
    val input = object:TCBuildType("q"){ }.apply {
      parameters {
        param("artifactRules", "\nworker/worker-server/logs => %logs%.zip\n")
      }
    }

    val x = kotlinWriter {
      paramsWithSpec(input.parameters)
    }

    println(x)
    Assert.assertTrue("should be 3++", x.filter { it == '+' }.count() == 3)
  }

  @Test
  fun should_generate_new_dependency_newlines() {
    val input = object:TCBuildType("q"){ }.apply {
      dependency(UnknownBuild("x")) {
        artifact {
          artifactPattern = "a\r\nb\r\nc\r\n"
        }
      }
    }

    val x = kotlinWriter {
      generateDependencies(context, input)
    }

    println(x)


    Assert.assertTrue(x.contains("rule"))
    Assert.assertTrue(x.contains("rule(\"b\")"))
    Assert.assertTrue(x.contains("rule(\"c\")"))
    Assert.assertTrue(x.contains("rule(\"\")"))
  }

}

