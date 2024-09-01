package promitech.colonization.ai.military.rebalance

class InfluenceMap(graph: Graph, sourceNodesId: List<String>) {
    private val mapDistance = mutableMapOf<String, Int>()

    init {
        val pool = ArrayList<String>()
        for (sourceNodeId in sourceNodesId) {
            pool.add(sourceNodeId)
            mapDistance.put(sourceNodeId, 0)
        }

        while (pool.isNotEmpty()) {
            val nodeZeroId: String = pool.removeAt(0)
            val nodeZeroDistance: Int = mapDistance[nodeZeroId]!!

            val neighbourEdges = graph.neighbourEdges(nodeZeroId)
            for ((neighbourNodeId: String, neighbourEdgeDistance: Int) in neighbourEdges) {
                val knownDistanceToNeighbour = mapDistance.getOrElse(neighbourNodeId) { Int.MAX_VALUE }
                val distanceToNeighbour = nodeZeroDistance + neighbourEdgeDistance
                if (distanceToNeighbour < knownDistanceToNeighbour) {
                    mapDistance[neighbourNodeId] = distanceToNeighbour
                    pool.add(neighbourNodeId)
                }
            }
        }
    }

    /**
     * Return distance to node, or Int.MAX_VALUE when unknown
     */
    fun distance(nodeId: String): Int {
        return mapDistance.getOrElse(nodeId) { Int.MAX_VALUE }
    }
}