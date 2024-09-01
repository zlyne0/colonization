package promitech.colonization.ai.military.rebalance

import org.junit.jupiter.api.Test
import promitech.colonization.ai.military.rebalance.GraphAssert.Companion.assertThat

class BalanceTest {

    /*
    @startuml
    left to right direction
    (a: 2) as a
    (b: 2) as b
    (c: -10) as c
    a -- b: 1
    b -- c: 1
    @enduml
     */
    @Test
    fun `rebalance 0`() {
        // given
        val graph = Graph()
        graph.addEdge("a", "b", 1)
        graph.addEdge("b", "c", 1)
        graph.nodeWeight("a", 2)
        graph.nodeWeight("b", 2)
        graph.nodeWeight("c", -10)

        // when
        RebalanceGenerator(graph).step()

        // then
        printNodeWeights(graph)
        assertThat(graph)
            .hasNodeWeight("a", 0)
            .hasNodeWeight("b", 0)
            .hasNodeWeight("c", -6)
    }

    /*
    @startuml
    left to right direction
    (a: 2) as a
    (a': 2) as aa #red;line.dotted
    (b: 1) as b
    (c: -1) as c
    (d: -2) as d
    (e: 2) as e
    a -- b: 1
    aa -- b #red;line.dotted;text:red  : 1
    b -- c: 1
    c -- d: 1
    c -- e: 3
    @enduml
     */
    @Test
    fun `rebalance 1`() {
        // given
        val graph = Graph()
        graph.addEdge("a", "b", 1)
        graph.addEdge("b", "c", 1)
        graph.addEdge("c", "d", 1)
        graph.addEdge("c", "e", 1)
        graph.nodeWeight("a", 2)
        graph.nodeWeight("b", 1)
        graph.nodeWeight("c", -1)
        graph.nodeWeight("d", -2)
        graph.nodeWeight("e", 2)

        // when
        val rebalanceGenerator = RebalanceGenerator(graph)
        rebalanceGenerator.step()
        printNodeWeights(graph)

        // then
        assertThat(graph)
            .hasNodeWeight("a", 1)
            .hasNodeWeight("b", 1)
            .hasNodeWeight("c", 1)
            .hasNodeWeight("d", -2)
            .hasNodeWeight("e", 1)
    }

    /*
    @startuml
    left to right direction
    (a: -2) as a
    (b: 0) as b
    (c: 2) as c
    (d: 2) as d
    (e: 0) as e
    (f: -1) as f
    a -- b: 1
    b -- c: 1
    b -- d: 1
    c -- e: 1
    e -- f: 1
    d -- f: 3
    @enduml
     */
    @Test
    fun `rebalance 2`() {
        // given
        val graph = Graph()
        graph.addEdge("a", "b", 1)
        graph.addEdge("b", "c", 1)
        graph.addEdge("b", "d", 1)
        graph.addEdge("c", "e", 1)
        graph.addEdge("e", "f", 1)
        graph.addEdge("d", "f", 3)

        graph.nodeWeight("a", -2)
        graph.nodeWeight("b", 0)
        graph.nodeWeight("c", 2)
        graph.nodeWeight("d", 2)
        graph.nodeWeight("e", 0)
        graph.nodeWeight("f", -1)

        // when
        val rebalanceGenerator = RebalanceGenerator(graph, true)
        rebalanceGenerator.step()
        printNodeWeights(graph)

        // then
        assertThat(graph)
            .hasNodeWeight("a", -1)
            .hasNodeWeight("b", 1)
            .hasNodeWeight("c", 1)
            .hasNodeWeight("d", 1)
            .hasNodeWeight("e", 0)
            .hasNodeWeight("f", -1)
    }

    /*
    @startuml
    left to right direction
    (a: 2) as a
    (b: 2) as b
    (c: -1) as c
    a -- b: 1
    b -- c: 1
    @enduml
     */
    @Test
    fun `rebalance 3`() {
        // given
        val graph = Graph()
        graph.addEdge("a", "b", 1)
        graph.addEdge("b", "c", 1)
        graph.nodeWeight("a", 2)
        graph.nodeWeight("b", 2)
        graph.nodeWeight("c", -1)

        // when
        val rebalanceGenerator = RebalanceGenerator(graph, true)
        rebalanceGenerator.step()
        printNodeWeights(graph)

        // then
        assertThat(graph)
            .hasNodeWeight("a", 2)
            .hasNodeWeight("b", 2)
            .hasNodeWeight("c", -1)
    }

    /*
    @startuml
    left to right direction
    (a: 2) as a
    (b: 0) as b
    (c: -1) as c
    a -- b: 1
    b -- c: 1
    @enduml
     */
    @Test
    fun `rebalance 4`() {
        val graph = Graph()
        graph.addEdge("a", "b", 1)
        graph.addEdge("b", "c", 1)
        graph.nodeWeight("a", 2)
        graph.nodeWeight("b", 2)
        graph.nodeWeight("c", -1)

        // when
        val rebalanceGenerator = RebalanceGenerator(graph, true)
        rebalanceGenerator.step()
        printNodeWeights(graph)

        // then
        assertThat(graph)
            .hasNodeWeight("a", 2)
            .hasNodeWeight("b", 2)
            .hasNodeWeight("c", -1)
    }

    /*
    @startuml
    left to right direction
    (a: 2) as a
    (b: 0) as b
    (c: -1) as c
    (e: 1) as e
    (f: -1) as f
    a -- b: 1
    b -- c: 1
    e -- b: 1
    b -- f: 1
    @enduml
     */
    @Test
    fun `rebalance 5`() {
        val graph = Graph()
        graph.addEdge("a", "b", 1)
        graph.addEdge("b", "c", 1)
        graph.addEdge("e", "b", 1)
        graph.addEdge("b", "f", 1)
        graph.nodeWeight("a", 2)
        graph.nodeWeight("b", 0)
        graph.nodeWeight("c", -1)
        graph.nodeWeight("e", 1)
        graph.nodeWeight("f", -1)

        // when
        val rebalanceGenerator = RebalanceGenerator(graph, true)
        rebalanceGenerator.step()
        printNodeWeights(graph)

        // then
        assertThat(graph)
            .hasNodeWeight("a", 1)
            .hasNodeWeight("b", 1)
            .hasNodeWeight("c", -1)
            .hasNodeWeight("e", 1)
            .hasNodeWeight("f", -1)
    }

    fun printNodeWeights(graph: Graph) {
        var str = ""
        for ((nodeId, weight) in graph.nodeWeights()) {
            if (str.isNotEmpty()) {
                str += ", "
            }
            str += "$nodeId: $weight"
        }
        println(str)
    }
}


