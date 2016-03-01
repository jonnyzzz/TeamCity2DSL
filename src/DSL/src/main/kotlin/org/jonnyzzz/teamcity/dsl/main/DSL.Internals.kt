package org.jonnyzzz.teamcity.dsl.main

import org.jonnyzzz.teamcity.dsl.api.TCDSLLazy
import org.jonnyzzz.teamcity.dsl.api.TCUUIDs
import org.jonnyzzz.teamcity.dsl.api.internal.DSLRegistryFacade
import org.jonnyzzz.teamcity.dsl.deleteAll
import org.jonnyzzz.teamcity.dsl.generating.DSLGenerating
import org.jonnyzzz.teamcity.dsl.generating.DSLOptions
import org.jonnyzzz.teamcity.dsl.model.TCProject
import org.jonnyzzz.teamcity.dsl.model.TCUUID
import org.jonnyzzz.teamcity.dsl.suppressing
import org.jonnyzzz.teamcity.dsl.xml.XmlGenerating
import org.jonnyzzz.teamcity.dsl.xml.XmlParsing
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import java.io.File
import java.lang.reflect.Modifier
import java.util.*

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 * on 06/09/14.
 */


//Used via java services
class DSLRegistryFacadeProxy : DSLRegistryFacade by DSLRegistry


object DSLRegistry : DSLRegistryFacade {
  val projects : MutableMap<String, MutableList<TCProject>> = linkedMapOf()
  val callbacks : MutableMap<String, MutableList<OnCompleteCallback>> = linkedMapOf()

  fun inferCalleeClass() : String {
    val clazz = Thread
            .currentThread()
            .getStackTrace()
            .asSequence()
            .drop(3)
            .firstOrNull { !it.getClassName().startsWith("org.jonnyzzz.teamcity.dsl.") && !it.getClassName().startsWith("kotlin.") }
            ?: throw Error("Failed to resolve callee package. Incorrect/empty package was used?")
    return clazz.getClassName()
  }

  override fun addCompletedProject(p : TCProject) : Unit {
    val clazz = inferCalleeClass()
    val map = projects.getOrPut(clazz) { LinkedList() }

    map.add(p)
    println("Registered project: ${p.id}")
  }

  class OnCompleteCallback(val callback : (TCUUID) -> Unit) {
    fun action(uuids : List<TCUUID>) {
      uuids.forEach { callback(it) }
    }
  }

  override fun addOnCompletedUUIDCallback(callback : (TCUUID) -> Unit) {
    val clazz = inferCalleeClass()
    callbacks.getOrPut(clazz) { LinkedList() }.add(OnCompleteCallback(callback))
  }

  private fun ensureClassesInitialized(parts : Collection<Class<Any>>) {
    parts.forEach {
      println("Loading part: $it...")
      suppressing {
        with(it.getField("INSTANCE$")){
          setAccessible(true)
          get(null)?.toString()
        }
      }
      suppressing {
        with(it.getField("INSTANCE")){
          setAccessible(true)
          get(null)?.toString()
        }
      }
      suppressing {
        with(it.getField("instance$")){
          setAccessible(true)
          get(null)?.toString()
        }
      }
      suppressing {
        it.newInstance().toString()
      }
      suppressing {
        it.getDeclaredMethods()
                .asSequence()
                .filter { it.getParameterTypes().isEmpty() && it.getModifiers().let { Modifier.isStatic(it) } }
                .take(3)
                .forEach { suppressing { it.invoke(null)?.toString() } }
      }
    }
  }

  fun <T> allTypes(clazz : Class<T>) : Set<Class<Any>>
          = (listOf(clazz as Class<Any>) + (listOf(clazz.getSuperclass()) + clazz.getInterfaces().toList()).filterNotNull().flatMap { allTypes(it as Class<Any>) }).toSet()

  fun getAllClassesFromPackage(pkg : String,
                               clazzLoader : ClassLoader) : Set<Class<Any>> {
    println("Scanning classes for DSL...")
    val scan = Reflections(
            clazzLoader,
            SubTypesScanner(false),
            pkg
    )

    var result = (allTypes(Any::class.java) + allTypes(TCUUID::class.java) + allTypes(TCUUIDs::class.java)).toSet()

    while(true) {
      val tmp = (result + result.flatMap { scan.getSubTypesOf(it).map { it as Class<Any> } }).toSet()
      if (tmp == result) break
      result = tmp
    }

    val parts =
            result
                    .filter {
                      val p = it.getPackage()?.getName()
                      when {
                        p == null -> false
                        p.equals(pkg) -> true
                        p.startsWith(pkg + ".") -> true
                        else -> false
                      }
                    }.toSet()
    return parts
  }

  interface ClassesMapFilter {
    fun <T> filter(m: Map<String, T>): List<T>
    fun <T> filterAll(m: Map<String, Iterable<T>>): List<T> = filter(m).flatMap { it }
  }

  fun classesMapFiler(pkg : String, clazzLoader : ClassLoader) : ClassesMapFilter {
    val parts = getAllClassesFromPackage(pkg, clazzLoader)

    println("Detected ${parts.size} parts...")

    ensureClassesInitialized(parts)

    if (parts.isEmpty()) {
      println("!!!. DSL classes must be in root package '$pkg'!")
      return object : ClassesMapFilter {
        override fun <T> filter(m: Map<String, T>): List<T> = listOf()
      }
    }


    println("Registered generators in the model: ${projects.size}")
    println("From classes:")
    projects.keys.map{it}.toSortedSet().forEach {
      println("  $it")
    }
    println()

    val partsNames = parts.map { it.getName() }.toSet()

    return  object : ClassesMapFilter {
      override fun <T> filter(m: Map<String, T>): List<T> = m.filter { partsNames.contains(it.key) }.map { it.value }
    }
  }

  fun loadAll(pkg : String,
              clazzLoader : ClassLoader) : List<TCProject> {
    println("Scanning classes for DSL...")

    val partsFilter = classesMapFiler(pkg, clazzLoader)

    val result = sortedMapOf<String, TCProject>()
    partsFilter.filterAll(projects).forEach {
      val projectId = it.id
      if (result.containsKey(projectId)) {
        throw RuntimeException("Project ${it.id} has already been registered")
      }
      result[projectId] = it
    }

    val actualProjects = result.values.toList()

    initializeLazy(actualProjects)
    initializeLazy(actualProjects.flatMap { p -> p.vcsRoots + p.buildTypes + p.buildTemplates + p.metaRunners + p.pluginSettings })

    initializeUUIDs(
            actualProjects.flatMap { p -> listOf(p) + p.vcsRoots + p.buildTypes + p.buildTemplates + p.metaRunners + p.pluginSettings },
            partsFilter.filterAll(callbacks)
            )

    return actualProjects
  }

  fun initializeLazy(objects: List<Any?>) {
    objects.filterNotNull()
            .filter { it is TCDSLLazy }
            .map { it as TCDSLLazy }
            .forEach {
              try {
                it.doLazyInit()
              } catch (t: Throwable) {
                throw Error("Failed to complete lazy-init for: $it. ${t.message}", t)
              }
            }
  }

  fun initializeUUIDs(objects: List<Any?>, callbacks: List<OnCompleteCallback>) {
    val uuids = objects.filterNotNull()
            .filter { it is TCUUID }
            .map { it as TCUUID }

    callbacks.forEach {
      try {
        it.action(uuids)
      } catch (t: Throwable) {
        throw Error("Failed to complete uuids-init for: $it. ${t.message}", t)
      }
    }
  }

  fun generateProjects(root : File,
                       clazzLoader : ClassLoader,
                       pkg : String = "org.jonnyzzz.teamcity.autodsl") {

    println("Loading...")
    println("Scanning classes from package: $pkg...")
    val projects = loadAll(pkg, clazzLoader)
    println("Generating TeamCity .xml files for the model of ${projects.size} project(s)")
    println("Generate files to $root")
    XmlGenerating.generate(projects.toList(), root)
    println("Completed")
  }
}
private class M

fun generateProjects(root : File,
                            pkg : String = "org.jonnyzzz.teamcity.autodsl",
                            clazzLoader : ClassLoader = M::class.java.getClassLoader()!!) : Unit {
  DSLRegistry.generateProjects(root, clazzLoader, pkg)
}

fun importProjects(xmlRoot : File, pkg : String, destRoot : File) : Unit {
  println("Loading model from XML files...")
  val model = XmlParsing.parse(xmlRoot)
  println("Parsed ${model.size} project(s).")
  println("Generating DSL code...")

  val dest = destRoot.getCanonicalFile()
  dest.deleteAll()
  dest.mkdirs()

  var options = DSLOptions().apply {
    packageName = pkg
  }
  DSLGenerating.generate(model, dest, options)

  println("DSL files were generated")
}

fun loadSave(xmlRoot : File, destRoot : File) : Unit {
  println("Loading model from XML files...")
  val model = XmlParsing.parse(xmlRoot)
  println("Parsed ${model.size} project(s).")
  println("Generating XML files...")

  val dest = destRoot.getCanonicalFile()
  dest.deleteAll()
  dest.mkdirs()

  XmlGenerating.generate(model, dest)
  println("XML files were generated")
}
