package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.*
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML
import org.jonnyzzz.kotlin.xml.bind.jdom.XUnknown


abstract class TCBuildTypeSettings : TCBuildSettings() {
  var templateId : String?
    get() = templateIdImpl
    set(value) {
      templateIdImpl = value
      updateOrderAttribute()
    }

  var runnersOrder : List<String>?
    get() = runnersOrderImpl?.let { it.split(", ".toRegex()).toTypedArray() }?.toList()
    set(value) {
      runnersOrderCache = value
      updateOrderAttribute()
    }

  private fun updateOrderAttribute() {
    if (templateId != null) {
      runnersOrderImpl = runnersOrderCache?.joinToString(", ")
    } else {
      runnersOrderImpl = null
    }
  }

  private var runnersOrderCache : List<String>? = null
  private var runnersOrderImpl by JXML[0xc404] / "settings" / "build-runners" / XAttribute("order")
  private var templateIdImpl by JXML[0xc400] / "settings" / XAttribute("ref")
}

abstract class TCBuildTemplateSettings : TCBuildSettings()

abstract class TCMetaRunnerSettings : TCBuildSettings() {
  init {
    buildTriggers = null
    vcs = null
  }
}

class TCRequirement {
  var id by JXML / XAttribute("id") - null
  var type by JXML / XName
  var name by JXML / XAttribute("name")
  var value by JXML / XAttribute("value")
}

abstract class TCBuildSettings {
  var options by JXML[0xc100] / "settings" / "options" / XSub(TCSettingsOptions::class.java)
  var disabledSettings by JXML[0xc200] / "settings" / "disabled-settings" / XElements("setting-ref") / XAttribute("ref")
  var parameters by JXML[0xc300] / "settings" / "parameters" / XElements("param") / XSub(TCParameterWithSpec::class.java) - listOf()
  var runners by JXML[0xc400] / "settings" / "build-runners" / XElements("runner") / XSub(TCSettingsRunner::class.java) - listOf()
  var vcs by JXML[0xc500] / "settings" / "vcs-settings" / XElements("vcs-entry-ref") / XSub(TCSettingsVCSRef::class.java)
  var requirements by JXML[0xc600] / "settings" / "requirements" / XAnyElements / XSub(TCRequirement::class.java)
  var buildTriggers by JXML[0xc700] / "settings" / "build-triggers" / XElements("build-trigger") / XSub(TCSettingsTrigger::class.java)
  var extensions by JXML[0xc800] / "settings" / "build-extensions" / XElements("extension") / XSub(TCSettingsExtension::class.java)
  var artifactDependencies by  JXML[0xc900] / "settings" / "artifact-dependencies" / XElements("dependency") / XSub(TCSettingsArtifactDependency::class.java)
  var snapshotDependencies by JXML[0xca00] / "settings" / "dependencies" / XElements("depend-on") / XSub(TCSettingsSnapshotDependency::class.java)
  var cleanup by JXML[0xcb00] / "settings" / "cleanup" / XUnknown
}
