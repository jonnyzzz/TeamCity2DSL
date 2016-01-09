package org.jonnyzzz.teamcity.dsl

import org.jdom2.Element
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import org.jonnyzzz.kotlin.xml.bind.XElements
import org.jonnyzzz.kotlin.xml.bind.XRoot
import org.jonnyzzz.kotlin.xml.bind.XSub
import org.jonnyzzz.kotlin.xml.bind.jdom.JDOM
import org.jonnyzzz.kotlin.xml.bind.jdom.JXML
import org.jonnyzzz.teamcity.dsl.api.param
import org.jonnyzzz.teamcity.dsl.clustering.clusteringRunnersByParameters
import org.jonnyzzz.teamcity.dsl.model.TCSettingsRunner
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.arrayListOf
import kotlin.collections.map
import kotlin.collections.toList


class ParametersClusteringTest {

  @Test
  fun test_cluster_same_one_parameter() {
    doClusterTest(
            runners {
              runner { param("a", "b") }
              runner { param("a", "b") }
            },
            runners {
            }
    )
  }

  @Test
  fun test_cluster_nothing_common() {
    doClusterTest(
            runners {
              runner { param("a", "b") }
              runner { param("c", "d") }
              runner { param("e", "f") }
            },
            runners {})
  }

  @Test
  fun test_cluster_same_several_parameter_diff() {
    doClusterTest(
            runners {
              runner {
                param("a", "b")
                param("c", "d")
                param("d", "q")
              }
              runner {
                param("a", "b")
                param("c", "q")
                param("d", "q")
              }
            },
            runners {
              runner {
                param("a", "b")
                param("d", "q")
              }
            }
    )
  }

  @Test
  fun test_cluster_big_group() {
    doClusterTest(
            runners {
              runner {
                param("q", "b")

                param("a", "b")
                param("c", "d")
                param("e", "f")
              }
              runner {
                param("q", "r")

                param("a", "b")
                param("c", "d")
                param("e", "f")
              }
              runner {
                param("q", "h")

                param("a", "b")
                param("c", "d")
                param("e", "f")
              }
              runner {
                param("a", "y")
                param("c", "q")
              }
            },
            runners {
              runner {
                param("a", "b")
                param("c", "d")
                param("e", "f")
              }
            }
    )
  }

  @Test
  fun test_cluster_2_groups() {
    doClusterTest(
            runners {
              runner {
                param("q", "b")

                param("a", "b")
                param("c", "d")
                param("e", "f")
              }
              runner {
                param("q", "r")

                param("a", "b")
                param("c", "d")
                param("e", "f")
              }
              runner {
                param("q", "h")

                param("a", "b")
                param("c", "d")
                param("e", "f")
              }
              runner {
                param("a", "y")
                param("c", "q")
              }
              runner {
                param("a", "y")
                param("c", "q")
              }
              runner {
                param("a", "y")
                param("c", "q")
              }
            },
            runners {
              runner {
                param("a", "b")
                param("c", "d")
                param("e", "f")
              }
              runner {
                param("a", "y")
                param("c", "q")
              }
            }
    )
  }

  @Test
  fun test_cluster_no_parameters() {
    doClusterTest(
            runners {
              runner { }
              runner { }
            },
            runners { }
    )
  }

  private val idGenerator = AtomicInteger()

  interface RunnersBuilder {
    fun runner(builder: TCSettingsRunner.() -> Unit)
  }

  private fun runners(builder: RunnersBuilder.() -> Unit): List<TCSettingsRunner> {
    val result = arrayListOf<TCSettingsRunner>()
    object : RunnersBuilder {
      override fun runner(builder: TCSettingsRunner.() -> Unit) {
        result.add(having(TCSettingsRunner()) { this.id = "${idGenerator.incrementAndGet()}"; builder() })
      }
    }.builder()
    return result
  }

  private fun doClusterTest(input: List<TCSettingsRunner>, clusters: List<TCSettingsRunner>) {
    fun Element.toText() = XMLOutputter(having(Format.getPrettyFormat()) { setIndent("  ") }).outputString(this)
    @XRoot("a") class V {
      var X by JXML / XElements("r") / XSub(TCSettingsRunner::class.java)
    }

    fun save(x: List<TCSettingsRunner>) = JDOM.save(
            having(V()) {
              X = x.map { having(JDOM.clone(it)) { id = null } }.toList()
            }
    ).toText()

    val gold = save(clusters)
    val actual = save(clusteringRunnersByParameters(input).map { having(TCSettingsRunner(), it) })

    println("Collected groups:\n$actual\n")
    Assert.assertEquals(gold, actual)
  }

}
