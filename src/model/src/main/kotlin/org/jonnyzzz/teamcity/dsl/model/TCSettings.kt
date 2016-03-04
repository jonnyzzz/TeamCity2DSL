package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.*
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML
import org.jonnyzzz.kotlin.xml.bind.jdom.XUnknown


class TCBuildTypeSettings : TCBuildSettings() {
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
  private var runnersOrderImpl by JXML[0x404] / "build-runners" / XAttribute("order")
  private var templateIdImpl by JXML[0x400] / XAttribute("ref")
}

class TCBuildTemplateSettings : TCBuildSettings()

class TCMetaRunnerSettings : TCBuildSettings() {
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

open class TCBuildSettings {
  var options by JXML[0x100] / "options" / XSub(TCSettingsOptions::class.java)
  var disabledSettings by JXML[0x200] / "disabled-settings" / XElements("setting-ref") / XAttribute("ref")
  var parameters by JXML[0x300] / "parameters" / XElements("param") / XSub(TCParameterWithSpec::class.java) - listOf()
  var runners by JXML[0x400] / "build-runners" / XElements("runner") / XSub(TCSettingsRunner::class.java) - listOf()
  var vcs by JXML[0x500] / "vcs-settings" / XElements("vcs-entry-ref") / XSub(TCSettingsVCSRef::class.java) - listOf()
  var requirements by JXML[0x600] / "requirements" / XAnyElements / XSub(TCRequirement::class.java) - listOf()
  var buildTriggers by JXML[0x700] / "build-triggers" / XElements("build-trigger") / XSub(TCSettingsTrigger::class.java)
  var extensions by JXML[0x800] / "build-extensions" / XElements("extension") / XSub(TCSettingsExtension::class.java)
  var artifactDependencies by  JXML[0x900] / "artifact-dependencies" / XElements("dependency") / XSub(TCSettingsArtifactDependency::class.java)
  var snapshotDependencies by JXML[0xa00] / "dependencies" / XElements("depend-on") / XSub(TCSettingsSnapshotDependency::class.java)
  var cleanup by JXML[0xb00] / "cleanup" / XUnknown
}

interface TCWithSettings {
  val settings: TCBuildSettings
}
