package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.XAttribute
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

class TCSettingsArtifactDependency {
  var id by JXML / XAttribute("id") - null
  var buildTypeId by JXML / XAttribute("sourceBuildTypeId")
  var cleanDestination by JXML / XAttribute("cleanDestination")
  var cleanDestinationAfterBuild by JXML / XAttribute("cleanDestinationAfterBuild")
  var revisionRuleName by JXML / "revisionRule" /  XAttribute("name")
  var revision by JXML / "revisionRule" / XAttribute("revision")
  var branch by JXML /"revisionRule"/ XAttribute("branch")
  var artifactPattern by JXML / "artifact" / XAttribute("sourcePath")
}

class TCSettingsSnapshotDependency {
  var buildTypeId by JXML / XAttribute("sourceBuildTypeId")
  var options by JXML / "options" / XSub(TCSettingsOptions::class.java)
}
