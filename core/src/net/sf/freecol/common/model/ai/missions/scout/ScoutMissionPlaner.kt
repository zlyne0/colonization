package net.sf.freecol.common.model.ai.missions.scout

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.map.InfluenceMap
import net.sf.freecol.common.model.map.path.Path
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.util.CollectionUtils
import promitech.colonization.screen.debug.TileDebugView

sealed class ScoutDestination {
    class TheSameIsland(val tile: Tile, val path: Path) : ScoutDestination()
    class OtherIsland(val tile: Tile, val transferLocationPath: Path): ScoutDestination()
    class Lack : ScoutDestination()
}

class ScoutMissionPlaner(
    private val game: Game,
    private val pathFinder: PathFinder,
    private val pathFinder2: PathFinder
) {

    fun findScoutDestinationFromLandTile(scout: Unit): ScoutDestination {
        pathFinder.generateRangeMap(game.map, scout, PathFinder.includeUnexploredTiles)

        var theBestTile: Tile? = findTheBestForRange { tile -> pathFinder.turnsCost(tile) }
        if (theBestTile != null) {
            return ScoutDestination.TheSameIsland(theBestTile, pathFinder.createPath(theBestTile))
        }
        // can not find destination on the same island
        theBestTile = findInOtherIsland(scout)
        if (theBestTile != null) {
            var transferLocationPath: Path? = settlementAsTransferLocation(scout)
            if (transferLocationPath == null) {
                transferLocationPath = seaSizeAsTransferLocation(scout)
            }
            if (transferLocationPath == null) {
                return ScoutDestination.Lack()
            }
            return ScoutDestination.OtherIsland(theBestTile, transferLocationPath)
        }
        return ScoutDestination.Lack()
    }

    private fun settlementAsTransferLocation(scout: Unit): Path? {
        // find the closest settlement to scout
        // already generated range map
        var theBestTile: Tile? = null
        var theBestRange: Int = PathFinder.INFINITY
        for (settlement in scout.owner.settlements) {
            val cost = pathFinder.turnsCost(settlement.tile)
            if (theBestTile == null || cost < theBestRange) {
                theBestTile = settlement.tile
                theBestRange = cost
            }
        }
        if (theBestTile != null) {
            return pathFinder.createPath(theBestTile)
        }
        return null
    }

    fun seaSizeAsTransferLocation(scout: Unit): Path? {
        val scoutEmbarkGenerateRangeFlags = CollectionUtils.enumSet(PathFinder.includeUnexploredTiles, PathFinder.FlagTypes.AllowEmbark)
        pathFinder.generateRangeMap(game.map, scout, scoutEmbarkGenerateRangeFlags)
        pathFinder2.generateRangeMap(
            game.map,
            findCivilizationSources(scout.owner),
            pathFinder2.createPathUnit(scout.owner, Specification.instance.unitTypes.getById(UnitType.CARAVEL)),
            CollectionUtils.enumSet(PathFinder.includeUnexploredTiles, PathFinder.FlagTypes.AvoidDisembark)
        )
        val embarkLocation = pathFinder2.findFirstTheBestSumTurnCost(pathFinder, PathFinder.SumPolicy.PRIORITY_SUM)
        if (embarkLocation == null) {
            return null
        }
        return pathFinder.createPath(embarkLocation)
    }

    private fun findInOtherIsland(scout: Unit): Tile? {
        val influenceMap = InfluenceMap(game.map)
        influenceMap.generate(findCivilizationSources(scout.owner))
        return findTheBestForRange { tile -> influenceMap.range(tile) }
    }

    private fun findCivilizationSources(player: Player): List<Tile> {
        val sourceTiles = mutableListOf<Tile>()
        sourceTiles.add(game.map.getSafeTile(player.entryLocation))
        for (settlement in player.settlements) {
            sourceTiles.add(settlement.tile)
        }
        return sourceTiles
    }

    private inline fun findTheBestForRange(rangeFunction: (Tile) -> Int): Tile? {
        var theBestTile: Tile? = null
        var theBestTileRange : Int = Int.MAX_VALUE
        forAllScoutDestinations { tile ->
            val range = rangeFunction(tile)
            if (range < theBestTileRange) {
                theBestTile = tile
                theBestTileRange = range
            }
        }
        return theBestTile
    }

    private inline fun forAllScoutDestinations(tileConsumer: (Tile) -> kotlin.Unit) {
        for (y in 0 .. game.map.height-1) {
            for (x in 0 .. game.map.width-1) {
                val tile = game.map.getSafeTile(x, y)
                if (isScoutDestinationCandidate(tile)) {
                    tileConsumer(tile)
                }
            }
        }
    }

    private fun isScoutDestinationCandidate(tile: Tile): Boolean {
        return tile.hasLostCityRumour()
            || tile.hasSettlement() && tile.settlement.isIndianSettlement && !tile.settlement.asIndianSettlement().isScouted
    }

    fun printAllCandidates(tileDebugView: TileDebugView) {
        forAllScoutDestinations { tile ->
            tileDebugView.appendStr(tile.x, tile.y, "Dest")
        }
    }

    fun printFirstDestination(scout: Unit, mapTileDebugInfo: MapTileDebugInfo) {
        val destination = findScoutDestinationFromLandTile(scout)
        when (destination) {
            is ScoutDestination.TheSameIsland -> mapTileDebugInfo.appendStr(destination.tile.x, destination.tile.y, "First")
            is ScoutDestination.OtherIsland -> {
                mapTileDebugInfo.appendStr(destination.tile.x, destination.tile.y, "First")
                mapTileDebugInfo.appendStr(destination.transferLocationPath.endTile.x, destination.transferLocationPath.endTile.y, "Transfer")
            }
        }
    }

}