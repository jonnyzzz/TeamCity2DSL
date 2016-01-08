[![Build Status](https://travis-ci.org/jonnyzzz/TeamCity2DSL.svg)](https://travis-ci.org/jonnyzzz/TeamCity2DSL)

TeamCity2DSL
============

Experimental project aiming to build a Kotlin DSL for editing
and refactoring TeamCity's build configuration files.

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


Further Work
============
* Complete with Gradle plugin in order to
make it work as a refactoring/develpment tool
* Design and implement unification rules, so that
generated DSL was re-using similar things
* Introduce DSL extension plugins & APIs (to support build runner/features/vcs/... plugins presentation)
* Document DSL and mixins
* Add more integration tests
* Invite contributirs
* Have fun!
