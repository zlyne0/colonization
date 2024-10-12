package net.sf.freecol.common.model.ai.missions.scout

import com.badlogic.gdx.utils.IntIntMap
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Map.distance
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.map.InfluenceRangeMapBuilder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.player.PlayerExploredTiles
import promitech.map.IntIntArray
import promitech.map.forEach
import java.lang.IllegalArgumentException

class PatrolPlaner(val game: Game, val player: Player) {

    fun findPatrolDestination(): PatrolDestination {
        val areaToPatrol = generateAreaToPatrol()
        return findTheClosesSpot(areaToPatrol)
    }

    fun findTheClosesSpot(areaToPatrol: IntIntArray): PatrolDestination {
        var theOldestExploredTurn = PlayerExploredTiles.RECENTLY_EXPLORED
        var destTurn = theOldestExploredTurn
        var theBestRange = Int.MAX_VALUE
        var theBestDest: Tile? = null

        areaToPatrol.forEach { x, y, range ->
            val exploredTurnValue = player.playerExploredTiles.exploredTurn(x, y)
            if (PlayerExploredTiles.isExplored(exploredTurnValue)) {
                if (exploredTurnValue <= theOldestExploredTurn) {
                    theOldestExploredTurn = exploredTurnValue
                    if (range < theBestRange || exploredTurnValue < destTurn) {
                        destTurn = exploredTurnValue
                        theBestRange = range
                        theBestDest = game.map.getSafeTile(x, y)
                    }
                }
            }
        }
        return PatrolDestination.ofOtherIsland(theBestDest)
    }

    fun findPatrolDestination(sourceTile: Tile, patrolDestinationPolicy: PatrolDestinationPolicy): PatrolDestination {
        val areaToPatrol = generateAreaToPatrol()
        return findTheOldestVisitedSpot(areaToPatrol, sourceTile, patrolDestinationPolicy)
    }

    fun generateAreaToPatrol(): IntIntArray {
        val influenceMap = InfluenceRangeMapBuilder()
        influenceMap.init(game.map, { tile: Tile -> tile.type.isLand })
        for (settlement in player.settlements) {
            influenceMap.addRangeSource(settlement.tile)
        }
        influenceMap.generateRange(6)
        return influenceMap.getRangeMap()
    }

    private fun findTheOldestVisitedSpot(areaToPatrol: IntIntArray, sourceTile: Tile, patrolDestinationPolicy: PatrolDestinationPolicy): PatrolDestination {
        val theOldestExploredTurnPerIsland = generateOldestExploredTurnPerIsland(areaToPatrol)
        if (theOldestExploredTurnPerIsland.isEmpty) {
            return PatrolDestination.Lack()
        }
        val sourceTileIslandId = game.map.areaId(sourceTile.x, sourceTile.y)

        when (patrolDestinationPolicy) {
            PatrolDestinationPolicy.TheSameIsland -> {
                if (!theOldestExploredTurnPerIsland.containsKey(sourceTileIslandId)) {
                    return PatrolDestination.Lack()
                }
                val turn = theOldestExploredTurnPerIsland.get(sourceTileIslandId, Int.MAX_VALUE)
                val destTile = findTheClosestCandidate(areaToPatrol, sourceTile, IslandTurn(sourceTileIslandId, turn))
                return PatrolDestination.ofTheSameIsland(destTile)
            }
            PatrolDestinationPolicy.DifferentIslands -> {
                val islandTurn = findTheClosestIsland(sourceTileIslandId, theOldestExploredTurnPerIsland)
                if (islandTurn.islandId == sourceTileIslandId) {
                    val destTile = findTheClosestCandidate(areaToPatrol, sourceTile, islandTurn)
                    return PatrolDestination.ofTheSameIsland(destTile)
                } else {
                    val destTile = findTheClosestCandidate(areaToPatrol, sourceTile, islandTurn)
                    return PatrolDestination.ofOtherIsland(destTile)
                }
            }
        }
    }

    fun generateOldestExploredTurnPerIsland(areaToPatrol: IntIntArray): IntIntMap {
        val theOldestExploredTurnPerIsland = IntIntMap()
        areaToPatrol.forEach { x, y, _ ->
            val exploredTurnValue = player.playerExploredTiles.exploredTurn(x, y)
            if (PlayerExploredTiles.isExplored(exploredTurnValue)) {
                val areaId = game.map.areaId(x, y)
                val theOldestExploredTurn = theOldestExploredTurnPerIsland.get(areaId, Int.MAX_VALUE)

                if (exploredTurnValue < theOldestExploredTurn) {
                    theOldestExploredTurnPerIsland.put(areaId, exploredTurnValue.toInt())
                }
            }
        }
        return theOldestExploredTurnPerIsland
    }

    private fun findTheClosestIsland(sourceTileIslandId: Int, theOldestExploredTurnPerArea: IntIntMap): IslandTurn {
        val actualAreaExploredTurn = theOldestExploredTurnPerArea.get(sourceTileIslandId, Int.MAX_VALUE)

        var theBestAreaToPatrol = sourceTileIslandId
        var theBestAreaExploredTurn = actualAreaExploredTurn
        for (entry in theOldestExploredTurnPerArea) {
            if (entry.value + 15 < theBestAreaExploredTurn) {
                theBestAreaToPatrol = entry.key
                theBestAreaExploredTurn = entry.value
            }
        }
        return IslandTurn(theBestAreaToPatrol, theBestAreaExploredTurn)
    }

    private fun findTheClosestCandidate(areaToPatrol: IntIntArray, sourceTile: Tile, islandTurn: IslandTurn): Tile? {
        var distance = Int.MAX_VALUE
        var tile: Tile? = null
        areaToPatrol.forEach { x, y, value ->
            val exploredTurnValue = player.playerExploredTiles.exploredTurn(x, y)
            if (game.map.areaId(x, y) == islandTurn.islandId && exploredTurnValue.toInt() == islandTurn.exploredTurn) {
                val tmpDistance = distance(sourceTile.x, sourceTile.y, x, y)
                if (tile == null || tmpDistance < distance) {
                    distance = tmpDistance
                    tile = game.map.getSafeTile(x, y)
                }
            }
        }
        return tile
    }

    fun printOldestVisitedSpots(areaToPatrol: IntIntArray, tileDebugView: MapTileDebugInfo) {
        val oldestExploredTurnPerIsland = generateOldestExploredTurnPerIsland(areaToPatrol)
        println("oldestExploredTurnPerIsland = $oldestExploredTurnPerIsland")
        if (oldestExploredTurnPerIsland.isEmpty) {
            return
        }
        val theOldestExploredTurn = minValue(oldestExploredTurnPerIsland).toByte()
        println("the oldest explored turn $theOldestExploredTurn")
        areaToPatrol.forEach { x, y, value ->
            val exploredTurnValue = player.playerExploredTiles.exploredTurn(x, y)
            if (theOldestExploredTurn == exploredTurnValue) {
                tileDebugView.appendStr(x, y, "O")
            }
        }
    }

    fun printOldestVisitedSpotsPerIsland(areaToPatrol: IntIntArray, tileDebugView: MapTileDebugInfo) {
        val oldestExploredTurnPerIsland: IntIntMap = generateOldestExploredTurnPerIsland(areaToPatrol)
        areaToPatrol.forEach { x, y, value ->
            val exploredTurnValue = player.playerExploredTiles.exploredTurn(x, y)
            if (PlayerExploredTiles.isExplored(exploredTurnValue)) {
                val islandId = game.map.areaId(x, y)
                if (oldestExploredTurnPerIsland.containsKey(islandId)) {
                    val oldestTurnValueOnIsland = oldestExploredTurnPerIsland.get(islandId, -1)
                    if (exploredTurnValue == oldestTurnValueOnIsland.toByte()) {
                        tileDebugView.appendStr(x, y, "O$exploredTurnValue")
                    }
                }
            }
        }
    }

    fun minValue(map: IntIntMap): Int {
        if (map.isEmpty) {
            throw IllegalArgumentException("empty map no min value")
        }
        var tmpVal = Int.MAX_VALUE
        for (value in map.values()) {
            if (value < tmpVal) {
                tmpVal = value
            }
        }
        return tmpVal
    }

    data class IslandTurn(val islandId: Int, val exploredTurn: Int)

    enum class PatrolDestinationPolicy {
        TheSameIsland, DifferentIslands
    }

    sealed class PatrolDestination {
        class TheSameIsland(val tile: Tile): PatrolDestination()
        class OtherIsland(val tile: Tile): PatrolDestination()
        class Lack: PatrolDestination()

        companion object {
            fun ofTheSameIsland(tile: Tile?): PatrolDestination {
                if (tile == null) {
                    return Lack()
                }
                return TheSameIsland(tile)
            }

            fun ofOtherIsland(tile: Tile?): PatrolDestination {
                if (tile == null) {
                    return Lack()
                }
                return OtherIsland(tile)
            }
        }
    }

}