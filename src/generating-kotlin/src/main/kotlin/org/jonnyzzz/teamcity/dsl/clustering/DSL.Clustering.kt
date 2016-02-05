package org.jonnyzzz.teamcity.dsl.clustering

import com.google.common.collect.Iterators
import com.google.common.collect.PeekingIterator
import org.jdom2.Element
import org.jdom2.output.XMLOutputter
import org.jonnyzzz.kotlin.xml.bind.jdom.JDOM
import org.jonnyzzz.teamcity.dsl.generating.KotlinWriter
import org.jonnyzzz.teamcity.dsl.generating.block
import org.jonnyzzz.teamcity.dsl.having
import java.util.*
import kotlin.collections.*
import kotlin.comparisons.*

fun <T : Any> mixinIncluded(runner: T, mixin: T.() -> Unit): Boolean {
  fun Element.toText() = XMLOutputter().outputString(this) ?: ""
  fun T.saveToText() = JDOM.save(this).toText()

  return runner.saveToText() == having(JDOM.clone(runner), mixin).saveToText()
}

abstract class DSLClustering<D : Any, G : Any, P : Any, R : Any> {
  protected abstract fun extractPs(r: D) : List<P>
  protected abstract fun extractRs(r: D) : R?
  protected abstract fun builderPs(ps : List<P>) : G

  fun cluster(input : List<D>) : List<G> {
    if (input.size < 2) return listOf()

    val parameters =
            input.flatMap { runner -> extractRs(runner)?.let { r -> extractPs(runner).map { it to r} } ?: listOf() }
                    .groupBy { it.first }
                    .mapValues { it.value.map { it.second }.toSet() }
                    .filter { it.value.size >= 2 }
                    .toList()
                    .sortedWith(compareByDescending { it.second.size })

    val result = arrayListOf<G>()
    fun addResult(params : List<P>) {
      if (params.isEmpty()) return
      result.add(builderPs(params))
    }

    fun collectGroup(it : PeekingIterator<Pair<P, Set<R>>>) {
      if (!it.hasNext()) return

      val first = it.next()

      var Rs = first.second.toSet()
      var Ps = listOf(first.first)

      fun win(Ps : Collection<*>, Rs : Set<*>): Int = (Ps.size - 1) * Rs.size * Rs.size
      fun reportResult() {
        if (Rs.size > 1 && Ps.size > 1) {
          addResult(Ps)
        }
      }

      while(it.hasNext()) {
        val next = it.peek()

        val Ps1 = Ps.plus(next.first)
        val Rs1 = Rs.intersect(next.second)

        if (win(Ps, Rs) >= win(Ps1, Rs1)) {
          //drop too obvious groups
          reportResult()
          return
        }

        it.next()
        Ps = Ps1
        Rs = Rs1
      }
      reportResult()
    }

    having(Iterators.peekingIterator(parameters.iterator())) {
      while (hasNext()) collectGroup(this)
    }

    return result
  }

}


abstract class DSLClusteringGenerator<D : Any> {
  protected abstract fun nameDMixin(d: D): String
  protected abstract fun funDMixin(d: D): String
  protected abstract fun funD(d: D) : String
  protected abstract fun predefinedMixins() : LinkedHashMap<String, D.() -> Unit>
  protected abstract fun detectMixins(ds : List<D>) : List<D.() -> Unit>
  protected abstract fun newD() : D

  protected abstract fun KotlinWriter.generateImplementationBlock(item: D, baseItem: D)
  protected open fun KotlinWriter.generatePostBlock(item: D, baseItem: D) {}

  private fun KotlinWriter.generateRunnerInternal(runner: D,
                                          runnerCall : String,
                                          allMixins : LinkedHashMap<String, D.() -> Unit>) {

    val baseRunner = newD()
    val blockHeaderList = arrayListOf<String>()

    for ((k, v) in allMixins + predefinedMixins()) {
      if (mixinIncluded(runner, v) && !mixinIncluded(baseRunner, v)) {
        baseRunner.v()
        blockHeaderList.add(k)
      }
    }

    val blockHeader = blockHeaderList.fold(runnerCall) { r, a -> r + " + $a" } + when (blockHeaderList.isEmpty()) {
      true -> ""
      else -> " +"
    }

    block(blockHeader) {
      generateImplementationBlock(runner, baseRunner)
    }
    generatePostBlock(runner, baseRunner)
  }

  private fun KotlinWriter.collectMixins(runners : List<D>) : LinkedHashMap<String, D.() -> Unit> {
    val mixinsEx = linkedMapOf<String, D.() -> Unit>()

    var cnt = 1
    val blocks = detectMixins(runners)

    blocks.forEach { block ->
      val runner = having(newD(), block)
      val mix = nameDMixin(runner) + "${cnt++}"
      val funMixin = funDMixin(runner)

      generateRunnerInternal(runner, "val $mix = $funMixin", linkedMapOf())

      mixinsEx.put(mix, block)
    }

    return mixinsEx
  }

  fun KotlinWriter.generate(runners: List<D>?): KotlinWriter.(D) -> Unit {
    val mixins = collectMixins(runners ?: listOf())
    return {
      runner ->
      generateRunnerInternal(
              runner,
              funD(runner),
              mixins)
    }
  }

}
