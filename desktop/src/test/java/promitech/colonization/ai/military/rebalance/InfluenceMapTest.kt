package promitech.colonization.ai.military.rebalance

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InfluenceMapTest {

    /*
    @startuml
    top to bottom direction
    (a: 0) as a
    (b: 0) as b
    (c: 0) as c
    (d: 0) as d
    (x: 2) as x
    (y: -1) as y
    a -- b : 1
    a -- c : 1
    a -- d : 1
    b -- c : 1
    b -- d : 1
    c -- d : 1
    x -- a : 1
    d -- y : 1
    @enduml
     */
    @Test
    fun `should generate influence map`() {
        // given
        val graph = Graph()
        graph.addEdge("a", "b", 1)
        graph.addEdge("a", "c", 1)
        graph.addEdge("a", "d", 1)
        graph.addEdge("b", "c", 1)
        graph.addEdge("b", "d", 1)
        graph.addEdge("c", "d", 1)
        graph.addEdge("x", "a", 1)
        graph.addEdge("d", "y", 1)

        // when
        val influenceMap = InfluenceMap(graph, listOf("y"))

        // then
        assertThat(influenceMap.distance("y")).isEqualTo(0)
        assertThat(influenceMap.distance("d")).isEqualTo(1)
        assertThat(influenceMap.distance("c")).isEqualTo(2)
        assertThat(influenceMap.distance("b")).isEqualTo(2)
        assertThat(influenceMap.distance("a")).isEqualTo(2)
        assertThat(influenceMap.distance("x")).isEqualTo(3)
    }


}