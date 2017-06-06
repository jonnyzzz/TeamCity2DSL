[![Build Status](https://travis-ci.org/jonnyzzz/TeamCity2DSL.svg)](https://travis-ci.org/jonnyzzz/TeamCity2DSL)

TeamCity2DSL
============

*Experimental* project aiming to build a Kotlin API and generator for editing
and refactoring TeamCity's build configuration XML files.

**NOTE**. This is my (Eugene Petrenko) personal experiments with TeamCity XML configurations. I try different own ideas to represent XML settings with Kotlin code. This has nothing in common with any possible embedded implementaions of a DSL in existing or future versions of TeamCity.

Blog Posts & Ideas
==================

http://jonnyzzz.com/blog/2016/03/08/gradle-for-dsl/ 
 
http://jonnyzzz.com/blog/2016/09/02/dsl-building/ 

http://jonnyzzz.com/blog/2016/09/16/power-of-dsl/ 


License
=======

Apache 2.0

Working with the tool
=====================

We are not ging to provide a dedicated support for
existing build runners (and plugins). We aim supporting
generic (plugin independent) way to read and write
TeamCity configuration files.

The tool provides
* A generator from XML files into the DSL
* An evaluator for the DSL to generate XML files

By definition, a transition from XML -> DSL -> XML
should not introduce any changes

Gradle integration
==================

We provide a Gradle plugin to export `xml to dsl` and `dsl to xml` tasks. 
The plugin also takes care of all dependecnies, project configuration and all the stuff.

Use the following Gradle script to start with an existing TeamCity project files
```gradle
buildscript {
  repositories {
    jcenter()
    mavenCentral()
    maven { url "http://dl.bintray.com/jonnyzzz/maven" }
  }

  dependencies {
    classpath 'org.jonnyzzz.teamcity.dsl:gradle-plugin:<PLUGIN VERISON>'
  }
}

apply plugin: 'org.jonnyzzz.teamcity.dsl'
```

Next call `gradle xml2dsl` to have the DSL be generated for your XML project model. Next the 
call to `gradle dsl2xml` will make Gradle to compile DSL with Kotlin and to run the generation task.

Examples
=========

DLS for test-data projects are generated when 'test' task
is called on the root project. Examples are located
under `test-integration/test-*/generated/kotlin` folders

This is how Build Configuration is represented
```kotlin
val Build_Test001_Build = Project_projectX.build("Test001_Build") {
  name = "Build"
  description = ""
  param("Extra Parameter", "")
  options {
    executionTimeout = 88
  }
  runner("RUNNER_1", "gradle-runner") + normalStep + {
    name = ""
    param("ui.gradleRunner.gradle.tasks.names", "clean build")
    param("ui.gradleRunner.gradle.wrapper.useWrapper", "true")
  }
  extension("jetbrains.agent.free.space", "jetbrains.agent.free.space") {
    param("free-space-work", "3gb")
  }
  vcs(VCS_Test001_HttpsGithubComJonnyzzzTeamCity2DSL) { }
  requirements { }
  cleanup { }
  trigger("vcsTrigger", "vcsTrigger") {
    param("quietPeriodMode", "DO_NOT_USE")
  }
}
```

Reporting test data
===================

The project needs more TeamCity XML configuration files for integration tests. It's way better to 
use real examples than synthetic files. 

Creating more integration tests: (replace ``<NNN>`` with a number you like)
- create a folder ``tests-integration/test-<NNN>``
- copy you TeamCity project files into ``tests-integration/test-<NNN>/teamcity``
- create a test file ``tests-integration/test-NNN/src/test/kotlin/org/jonnyzzz/teamcity/dsl/util/Test<NNN>.kt``
- create a test class 
```kotlin
package org.jonnyzzz.teamcity.dsl.util

class Test<NNN> : IntegrationTestBase()
```
- run gradle project for test

Submitting test data is easy. You may either follow steps above or simply share 
your test data with me in any way you like.


Further Work
============
* (:+1:) Introduce Gradle plugin to simplify generations and to provide refactorings tool
* Design and implement unification rules, so that generated DSL was re-using similar things
* Introduce DSL extension plugins & APIs (to support build runner/features/vcs/... plugins presentation)
* Document DSL and mixins
* Blog on Kotlin code generation
* Add more integration tests (please share your TeamCity project configuration files with me!)
* Invite contributors
* Publish binaries to Maven repository / Gradle plugins repository
* Have fun!
