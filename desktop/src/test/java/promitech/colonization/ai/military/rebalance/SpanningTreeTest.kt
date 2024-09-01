package promitech.colonization.ai.military.rebalance

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SpanningTreeTest {

    /*
    @startuml
    left to right direction
    (a) as a
    (b)
    (c)
    (d)
    (e)
    (f)
    (x)
    (y)
    (z)
    a --> b : 1
    b --> c : 1
    b --> d : 1
    d --> e : 1
    c --> f : 1

    a --> x : 1
    a --> y : 1
    x --> z : 1
    y --> z : 1
    @enduml
     */
    @Test
    fun `should generate spanning tree`() {
        // given
        val graph = Graph()
        graph.addEdge("a", "b", 1)
        graph.addEdge("b", "c", 1)
        graph.addEdge("b", "d", 1)
        graph.addEdge("d", "e", 1)
        graph.addEdge("c", "f", 1)

        graph.addEdge("a", "x", 1)
        graph.addEdge("a", "y", 1)
        graph.addEdge("x", "z", 1)
        graph.addEdge("y", "z", 1)

        // when
        val spanningTree = SpanningTree(graph, "a")

        // then
        for (leaf in spanningTree.leafs()) {
            println(spanningTree.leafPathToString(leaf))
        }

        assertThat(spanningTree.leafs())
            .containsExactlyInAnyOrder("f", "e", "z", "y")
        assertThat(Iterable { spanningTree.pathFromLeafIterator("e") }.toList())
            .containsExactly("e", "d", "b", "a")
        assertThat(Iterable { spanningTree.pathFromLeafIterator("f") }.toList())
            .containsExactly("f", "c", "b", "a")
        assertThat(Iterable { spanningTree.pathFromLeafIterator("y") }.toList())
            .containsExactly("y", "a")
        assertThat(Iterable { spanningTree.pathFromLeafIterator("z") }.toList())
            .containsExactly("z", "x", "a")
    }

}