package org.jonnyzzz.teamcity.dsl.api

import org.jonnyzzz.teamcity.dsl.having
import org.jonnyzzz.teamcity.dsl.model.*
import org.jonnyzzz.teamcity.dsl.xml.generateXMLString
import kotlin.comparisons.compareBy
import kotlin.reflect.KMutableProperty1


fun TCBuildType.param(name : String, value : String? = null, builder : TCParameterWithSpecBuilder.() -> Unit = {}) {
  settings { param(name, value, builder) }
}

fun TCBuildTemplate.param(name : String, value : String? = null, builder : TCParameterWithSpecBuilder.() -> Unit = {}) {
  settings { param(name, value, builder) }
}

fun TCMetaRunner.param(name : String, value : String? = null, builder : TCParameterWithSpecBuilder.() -> Unit = {}) {
  settings { param(name, value, builder) }
}

fun TCBuildSettings.param(name : String, value : String? = null, builder : TCParameterWithSpecBuilder.() -> Unit = {}) {
  addParameterWithSpec(TCBuildSettings::parameters, name, value, builder)
}

fun TCProject.param(name : String, value : String? = null, builder : TCParameterWithSpecBuilder.() -> Unit = {}) : Unit {
  addParameterWithSpec(TCProject::parameters, name, value, builder)
}

fun TCSettingsRunner.param(name : String, value : String? = null, builder : TCParameterBuilder.() -> Unit = {}) {
  addParameter(TCSettingsRunner::parameters, name, value, builder)
  parameters = parameters?.sortedWith(compareBy { it.name ?: "" })
}

fun TCSettingsExtension.param(name : String, value : String? = null, builder : TCParameterBuilder.() -> Unit = {}) {
  addParameter(TCSettingsExtension::parameters, name, value, builder)
}

fun TCVCSRoot.param(name : String, value : String? = null, builder : TCParameterBuilder.() -> Unit = {}) {
  addParameter(TCVCSRoot::parameters, name, value, builder)
  parameters = parameters?.sortedWith(compareBy { it.name ?: "" })
}

fun ref(s : String) : String = "%$s%"

fun <T, P : TCParameter> T.addParameterImpl(parameters: KMutableProperty1<T, List<P>?>,
                                            name: String,
                                            value: String?,
                                            newParameter: () -> P,
                                            builder: TCParameterBuilder.(P) -> Unit) {
  addAbstractParameterImpl(parameters, name, value, newParameter, { p ->
    object : TCParameterBuilder, TCAbstractParameterBuilder by this {
      override fun legacyText() {
        p.escapeAsText = true
      }
    }.builder(p)
  })
}
fun <T, P : TCAbstractParam> T.addAbstractParameterImpl(parameters: KMutableProperty1<T, List<P>?>,
                                            name: String,
                                            value: String?,
                                            newParameter: () -> P,
                                            builder: TCAbstractParameterBuilder.(P) -> Unit) {
  val actualParameter = parameters.get(this)?.firstOrNull { it.name == name }
  val p = actualParameter ?: having(newParameter()) { this.name = name }
  p.value = value

  class XMLValue(var text : String) : TCParameterBuilderXMLValue
  object : TCAbstractParameterBuilder {
    override val value: TCParameterBuilderValue = object:TCParameterBuilderValue {}
    override fun xml(rootElementName: String, builder: TCParameterXMLBuilder.() -> Unit): TCParameterBuilderXMLValue
            = XMLValue(generateXMLString(elementImpl(rootElementName) {
      object : TCParameterXMLBuilder, TCUnknownBuilder by this {
      }.builder();
    }))

    operator override fun TCParameterBuilderValue.plusAssign(s: String) {
      p.value = if (p.value == null) s else p.value + "\n" + s
    }

    operator override fun TCParameterBuilderValue.plusAssign(s: TCParameterBuilderXMLValue) {
      plusAssign((s as XMLValue).text)
    }
  }.builder(p)

  if (actualParameter == null) {
    parameters.set(this, (parameters.get(this) ?: listOf()).filter { it.name != name } + p)
  }
}

fun <T> T.addParameter(parameters: KMutableProperty1<T, List<TCParameter>?>,
                                            name: String,
                                            value: String?,
                                            builder: TCParameterBuilder.() -> Unit) {
  addParameterImpl(parameters, name, value, { TCParameter() }, {
    builder()
  })
}

fun <T> T.addParameterWithSpec(parameters: KMutableProperty1<T, List<TCParameterWithSpec>?>,
                               name : String,
                               value : String?,
                               builder : TCParameterWithSpecBuilder.() -> Unit) {

  addParameterImpl(parameters, name, value, { TCParameterWithSpec() }, { p ->
    object : TCParameterWithSpecBuilder, TCParameterBuilder by this {
      override var spec: String?
        get() = p.spec
        set(v) { p.spec = v}
    }.builder()
  })
}

fun TCSettingsOptions.option(name : String, value : String? = null, builder : TCOptionBuilder.() -> Unit = {}) {
  val that = this
  class proxy {
    var options2 : List<TCSettingsOption>?
      get() = that.options
      set(v) { that.options = v}
  }

  proxy().addOption(proxy::options2, name, value, builder)
}

fun <T> T.addOption(parameters: KMutableProperty1<T, List<TCSettingsOption>?>,
                               name : String,
                               value : String?,
                               builder : TCOptionBuilder.() -> Unit) {

  addAbstractParameterImpl(parameters, name, value, { TCSettingsOption() }, { p ->
    object : TCOptionBuilder, TCAbstractParameterBuilder by this {
    }.builder()
  })
}

interface TCParameterBuilderValue
interface TCParameterBuilderXMLValue

interface TCAbstractParameterBuilder {
  val value: TCParameterBuilderValue

  operator fun TCParameterBuilderValue.plusAssign(s: String)
  operator fun TCParameterBuilderValue.plusAssign(s: TCParameterBuilderXMLValue)

  fun xml(rootElementName : String, builder : TCParameterXMLBuilder.() -> Unit) : TCParameterBuilderXMLValue
}

interface TCParameterXMLBuilder : TCUnknownBuilder {

}

interface TCOptionBuilder : TCAbstractParameterBuilder {

}

interface TCParameterBuilder : TCAbstractParameterBuilder {
  fun legacyText()
}

interface TCParameterWithSpecBuilder : TCParameterBuilder {
  var spec: String?
}
