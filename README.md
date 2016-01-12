[![Build Status](https://travis-ci.org/jonnyzzz/TeamCity2DSL.svg)](https://travis-ci.org/jonnyzzz/TeamCity2DSL)

TeamCity2DSL
============

*Experimental* project aiming to build a Kotlin API and generator for editing
and refactoring TeamCity's build configuration XML files.

**NOTE**. This is my (Eugene Petrenko) personal experiments with TeamCity XML configurations. I try different own ideas to represent XML settings with Kotlin code. This has nothing in common with any possible embedded implementaions of a DSL in existing or future versions of TeamCity.

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
should not introduce any changes.

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

Further Work
============
* Introduce Gradle plugin to simplify generations and to provide refactorings tool
* Design and implement unification rules, so that generated DSL was re-using similar things
* Introduce DSL extension plugins & APIs (to support build runner/features/vcs/... plugins presentation)
* Document DSL and mixins
* Blog on Kotlin code generation
* Add more integration tests (please share your TeamCity project configuration files with me!)
* Invite contributors
* Publish binaries to Maven repository / Gradle plugins repository
* Have fun!
