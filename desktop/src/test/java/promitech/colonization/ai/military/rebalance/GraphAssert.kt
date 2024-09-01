package promitech.colonization.ai.military.rebalance

import org.assertj.core.api.AbstractAssert

class GraphAssert(graph: Graph): AbstractAssert<GraphAssert, Graph>(graph, GraphAssert::class.java) {

    fun hasNodeWeight(nodeId: String, weight: Int): GraphAssert {
        val nodeWeight = actual.nodeWeight(nodeId)
        if (nodeWeight != weight) {
            failWithMessage("expected node \"$nodeId\" has weight \"$weight\" but has \"$nodeWeight\"")
        }
        return this
    }

    companion object {
        fun assertThat(graph: Graph): GraphAssert {
            return GraphAssert(graph)
        }
    }

}