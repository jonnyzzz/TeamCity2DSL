package org.jonnyzzz.teamcity.dsl.model

import org.jonnyzzz.kotlin.xml.bind.*
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML

class TCSettingsVCSRef {
  var rootId by JXML / XAttribute("root-id")
  var checkoutRule by JXML / XElements("checkout-rule") / XAttribute("rule")
}

interface TCVCSRootRef {
  val id : String?
}

@XRoot("vcs-root")
abstract class TCVCSRoot(override val id : String) : TCUUID, TCVCSRootRef {
  override var uuid by JXML[0x1000] / XAttribute("uuid") - null

  var vcsType by JXML[0x1100] / XAttribute("type")
  var modificationCheckInterval by JXML[0x1200] / XAttribute("modification-check-interval") - null
  var name by JXML / "name" / XText

  var parameters by JXML / XElements("param") / XSub(TCParameter::class.java)
}
