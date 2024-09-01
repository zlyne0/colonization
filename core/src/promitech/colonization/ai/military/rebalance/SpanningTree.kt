package promitech.colonization.ai.military.rebalance

class SpanningTree(graph: Graph, sourceNodeId: String) {
    private val edgesDirection = mutableMapOf<String, String>()
    private val sourceNodeDistance = mutableMapOf<String, Int>()
    private val leafs = mutableSetOf<String>()

    init {
        val pool = ArrayList<String>()
        pool.add(sourceNodeId)
        sourceNodeDistance.put(sourceNodeId, 0)

        while (pool.isNotEmpty()) {
            val nodeZeroId: String = pool.removeAt(0)
            val nodeZeroDistance: Int = sourceNodeDistance[nodeZeroId]!!

            val neighbourEdges = graph.neighbourEdges(nodeZeroId)
            for ((neighbourNodeId: String, neighbourEdgeDistance: Int) in neighbourEdges) {
                val knownDistanceToNeighbour = sourceNodeDistance.getOrElse(neighbourNodeId) { Int.MAX_VALUE }
                val distanceToNeighbour = nodeZeroDistance + neighbourEdgeDistance
                if (distanceToNeighbour < knownDistanceToNeighbour) {
                    sourceNodeDistance[neighbourNodeId] = distanceToNeighbour
                    pool.add(neighbourNodeId)
                    edgesDirection[neighbourNodeId] = nodeZeroId
                }
            }
        }

        determineLeafs(graph)
    }

    /**
     * Return distance to node, or Int.MAX_VALUE when unknown
     */
    fun distance(nodeId: String): Int {
        return sourceNodeDistance.getOrElse(nodeId) { Int.MAX_VALUE }
    }

    private fun determineLeafs(graph: Graph) {
        val sources = edgesDirection.keys
        val destinations = edgesDirection.values

        for (nodeId in graph.allNodesIds()) {
            if (sources.contains(nodeId) && !destinations.contains(nodeId)) {
                leafs.add(nodeId)
            }
        }
    }

    fun leafs(): Set<String> {
        return leafs
    }

    fun leafPathToString(nodeId: String): String {
        var str = ""
        for (middleNodeId in pathFromLeafIterator(nodeId)) {
            val distance = sourceNodeDistance.getOrElse(middleNodeId) { Int.MAX_VALUE }
            if (str.isNotEmpty()) {
                str += " -> "
            }
            str += "${middleNodeId}($distance)"
        }
        return str
    }

    fun pathFromLeafIterator(nodeId: String): Iterator<String> {
        return LeafToSourceNodeIterator(this, nodeId)
    }

    private class LeafToSourceNodeIterator(
        val spanningTree: SpanningTree,
        leafNodeId: String
    ): AbstractIterator<String>() {

        private var actualNodeId: String = leafNodeId

        init {
            setNext(actualNodeId)
        }

        override fun computeNext() {
            val tmpId = spanningTree.edgesDirection[actualNodeId]
            if (tmpId != null) {
                actualNodeId = tmpId
                setNext(tmpId)
            } else {
                done()
            }
        }
    }
}