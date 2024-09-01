package promitech.colonization.ai.military.rebalance

class RebalanceGenerator(private val graph: Graph, private val logTransitions: Boolean = false) {

    fun step() {
        val balanceLimit = calculateBalanceLimit(graph)
        val balanceLimitCeil = Math.ceil(balanceLimit).toInt()
        if (logTransitions) {
            println("balanceLimit = " + balanceLimit)
            println("balanceLimit.upper = " + balanceLimitCeil)
        }

        val negativeBalanceNodes = generateNodesWithNegativeBalance(balanceLimit)
        val influenceMap = InfluenceMap(graph, negativeBalanceNodes)

        val positiveBalanceNodes = generateNodesWithPositiveBalance(balanceLimit)

        val nodesToRebalancePool = mutableListOf<String>()
        nodesToRebalancePool.addAll(positiveBalanceNodes)

        while (nodesToRebalancePool.isNotEmpty()) {
            val firstNodeId = nodesToRebalancePool.removeAt(0)
            val rebalanceDestinationNode = rebalanceDirection(influenceMap, firstNodeId, balanceLimit)
            // destinationNodeWeight is under balance
            if (rebalanceDestinationNode != null) {

                val firstNodeWeight = graph.nodeWeight(firstNodeId)
                if (firstNodeWeight > 0) {
                    val reductionSize = Math.min(firstNodeWeight - balanceLimitCeil, firstNodeWeight)

                    graph.plusWeight(rebalanceDestinationNode, reductionSize)
                    graph.minusWeight(firstNodeId, reductionSize)
                    nodesToRebalancePool.add(rebalanceDestinationNode)
                    if (logTransitions) {
                        println("rebalance $reductionSize from: ${firstNodeId} to: ${rebalanceDestinationNode}")
                    }
                }

            } else {
                if (logTransitions) {
                    println("no rebalance from $firstNodeId")
                }
            }
        }
    }

    private fun rebalanceDirection(
        negativeBalanceInfluenceMap: InfluenceMap,
        sourceNodeId: String,
        balanceLimit: Double
    ): String? {
        val sourceNodeWeight = graph.nodeWeight(sourceNodeId)
        // chose neighbour node with
        // - balanceLimit lower then sourceNodeId
        // - the lower influence map
        // - weight lower then sourceNodeId
        var influenceMapWeight = Int.MAX_VALUE
        var nodeId: String? = null
        for ((nNodeId, nWeight) in graph.neighbourEdges(sourceNodeId)) {
            if (balanceLimit < nWeight.toDouble() &&
                sourceNodeWeight > nWeight &&
                (nodeId == null || influenceMapWeight > negativeBalanceInfluenceMap.distance(nNodeId))
            ) {
                nodeId = nNodeId
                influenceMapWeight = negativeBalanceInfluenceMap.distance(nNodeId)
            }
        }
        return nodeId
    }

    private fun generateNodesWithPositiveBalance(balanceLimit: Double): List<String> {
        val nodes = mutableListOf<String>()
        for ((nodeId, nodeWeight) in graph.nodeWeights()) {
            if (nodeWeight.toDouble() > balanceLimit) {
                nodes.add(nodeId)
            }
        }
        return nodes
    }

    private fun generateNodesWithNegativeBalance(balanceLimit: Double): List<String> {
        val negativeBalanceNodeIds = mutableListOf<String>()
        for ((nodeId, weight) in graph.nodeWeights()) {
            if (weight.toDouble() < balanceLimit) {
                negativeBalanceNodeIds.add(nodeId)
            }
        }
        return negativeBalanceNodeIds
    }

    private fun calculateBalanceLimit(graph: Graph): Double {
        var sum = 0
        for ((_, weight) in graph.nodeWeights()) {
            sum += weight
        }
        return sum.toDouble() / graph.nodeWeights().size
    }
}