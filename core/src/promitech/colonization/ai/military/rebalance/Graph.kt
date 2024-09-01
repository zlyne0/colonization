package promitech.colonization.ai.military.rebalance

import java.util.Collections

class Graph {

    private val emptyMap = Collections.unmodifiableMap(LinkedHashMap<String, Int>())

    private val edges = mutableMapOf<String, MutableMap<String, Int>>()
    private val nodeWeights = mutableMapOf<String, Int>()

    fun addNode(nodeId: String, nodeWeight: Int) {
        edges[nodeId] = LinkedHashMap()
        nodeWeights[nodeId] = nodeWeight
    }

    fun nodeWeight(nodeId: String, nodeWeight: Int) {
        nodeWeights[nodeId] = nodeWeight
    }

    fun nodeWeight(nodeId: String): Int {
        return nodeWeights[nodeId]!!
    }

    fun plusWeight(nodeId: String, value: Int) {
        nodeWeights[nodeId] = nodeWeights[nodeId]!! + value
    }

    fun minusWeight(nodeId: String, value: Int) {
        nodeWeights[nodeId] = nodeWeights[nodeId]!! - value
    }

    fun addEdge(a: String, b: String, weight: Int) {
        val aNodes = edges.getOrPut(a) { LinkedHashMap() }
        val bNodes = edges.getOrPut(b) { LinkedHashMap() }
        aNodes[b] = weight
        bNodes[a] = weight
    }

    fun neighbourEdges(nodeId: String): Map<String, Int> {
        return edges.getOrElse(nodeId) { emptyMap }
    }

    fun nodeWeights(): Map<String, Int> {
        return nodeWeights
    }

    fun allNodesIds(): Set<String> {
        return edges.keys
    }
}

