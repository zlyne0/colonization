package net.sf.freecol.common.model.ai.missions.scout

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.map.InfluenceMap
import net.sf.freecol.common.model.map.path.Path
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.util.CollectionUtils
import promitech.colonization.screen.debug.TileDebugView

sealed class ScoutDestination {
    class TheSameIsland(val tile: Tile, val path: Path) : ScoutDestination()
    class OtherIsland(val tile: Tile, val transferLocationPath: Path): ScoutDestination()
    class OtherIslandFromCarrier(val tile: Tile) : ScoutDestination()
    class AlreadOnShip(val tile: Tile)
    class Lack : ScoutDestination()
}

class ScoutMissionPlaner(
    private val game: Game,
    private val pathFinder: PathFinder,
    private val pathFinder2: PathFinder
) {

    private val maxRangeForScoutOnOtherIsland = 12

    fun findScoutDestination(scout: Unit): ScoutDestination {
        if (scout.isAtUnitLocation) {
            val theBestTile = findInOtherIsland(scout.owner)
            if (theBestTile == null) {
                return ScoutDestination.Lack()
            }
            return ScoutDestination.OtherIslandFromCarrier(theBestTile)
        }

        pathFinder.generateRangeMap(game.map, scout, PathFinder.includeUnexploredTiles)
        var theBestTile: Tile? = findTheBestForRange(scout.owner) { tile -> pathFinder.turnsCost(tile) }
        if (theBestTile != null) {
            return ScoutDestination.TheSameIsland(theBestTile, pathFinder.createPath(theBestTile))
        }

        // can not find destination on the same island
        theBestTile = findInOtherIsland(scout.owner)
        if (theBestTile != null) {
            var transferLocationPath: Path? = settlementAsTransferLocation(scout)
            if (transferLocationPath == null) {
                transferLocationPath = seaSideAsTransferLocation(scout)
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
            if (cost < theBestRange) {
                theBestTile = settlement.tile
                theBestRange = cost
            }
        }
        if (theBestTile != null) {
            return pathFinder.createPath(theBestTile)
        }
        return null
    }

    fun seaSideAsTransferLocation(scout: Unit): Path? {
        val scoutEmbarkGenerateRangeFlags = CollectionUtils.enumSum(PathFinder.includeUnexploredTiles, PathFinder.FlagTypes.AllowEmbark)
        pathFinder.generateRangeMap(game.map, scout, scoutEmbarkGenerateRangeFlags)
        pathFinder2.generateRangeMap(
            game.map,
            generateCivilizationSources(scout.owner),
            pathFinder2.createPathUnit(scout.owner, Specification.instance.unitTypes.getById(UnitType.CARAVEL)),
            CollectionUtils.enumSum(PathFinder.includeUnexploredTiles, PathFinder.FlagTypes.AvoidDisembark)
        )
        val embarkLocation = pathFinder2.findFirstTheBestSumTurnCost(pathFinder, PathFinder.SumPolicy.PRIORITY_SUM)
        if (embarkLocation == null) {
            return null
        }
        return pathFinder.createPath(embarkLocation)
    }

    private fun findInOtherIsland(player: Player): Tile? {
        val influenceMap = InfluenceMap(game.map, maxRangeForScoutOnOtherIsland)
        influenceMap.generate(generateCivilizationSources(player))
        val tileOnOtherIsland = findTheBestForRange(player) { tile -> influenceMap.range(tile) }
        if (tileOnOtherIsland == null) {
            return null
        }
        return tileOnOtherIsland
    }

    private fun generateCivilizationSources(player: Player): List<Tile> {
        val sourceTiles = mutableListOf<Tile>()
        sourceTiles.add(game.map.getSafeTile(player.entryLocation))
        for (settlement in player.settlements) {
            sourceTiles.add(settlement.tile)
        }
        return sourceTiles
    }

    private inline fun findTheBestForRange(player: Player, rangeFunction: (Tile) -> Int): Tile? {
        var theBestTile: Tile? = null
        var theBestTileRange : Int = Int.MAX_VALUE
        forAllScoutDestinations(player) { tile ->
            val range = rangeFunction(tile)
            if (range < theBestTileRange) {
                theBestTile = tile
                theBestTileRange = range
            }
        }
        return theBestTile
    }

    private inline fun forAllScoutDestinations(player: Player, tileConsumer: (Tile) -> kotlin.Unit) {
        val playerAiContainer = game.aiContainer.playerAiContainer(player)

        for (y in 0 .. game.map.height-1) {
            for (x in 0 .. game.map.width-1) {
                val tile = game.map.getSafeTile(x, y)
                if (isScoutDestinationCandidate(tile)) {
                    if (playerAiContainer.isScoutTileBlocked(tile)) {
                        continue
                    }
                    tileConsumer(tile)
                }
            }
        }
    }

    private fun isScoutDestinationCandidate(tile: Tile): Boolean {
        return tile.hasLostCityRumour()
            || tile.hasSettlement() && tile.settlement.isIndianSettlement && !tile.settlement.asIndianSettlement().isScouted
    }

    fun printAllCandidates(player: Player, tileDebugView: TileDebugView) {
        forAllScoutDestinations(player) { tile ->
            tileDebugView.appendStr(tile.x, tile.y, "Dest")
        }
    }

    fun printFirstDestination(scout: Unit, mapTileDebugInfo: MapTileDebugInfo) {
        val destination = findScoutDestination(scout)
        when (destination) {
            is ScoutDestination.TheSameIsland -> mapTileDebugInfo.appendStr(destination.tile.x, destination.tile.y, "First")
            is ScoutDestination.OtherIsland -> {
                mapTileDebugInfo.appendStr(destination.tile.x, destination.tile.y, "First")
                mapTileDebugInfo.appendStr(destination.transferLocationPath.endTile.x, destination.transferLocationPath.endTile.y, "Transfer")
            }
        }
    }

    fun prepareMission(player: Player, playerMissionContainer: PlayerMissionsContainer) {
        if (playerMissionContainer.isMissionTypeExists(ScoutMission::class.java)) {
            return;
        }
        if (player.units.size() < 3) {
            return;
        }

        createMissionFromUnusedUnits(player, playerMissionContainer);
        if (playerMissionContainer.isMissionTypeExists(ScoutMission::class.java)) {
            return;
        }

        if (!canAffordForScout(player)) {
            return;
        }
        val otherIslandDestination: Tile? = findInOtherIsland(player)
        if (otherIslandDestination == null) {
            return
        }
        val scout = buyScoutInEurope(player)
        val scoutMission = ScoutMission(scout)
        scoutMission.waitForTransport(otherIslandDestination)
        playerMissionContainer.addMission(scoutMission)
        playerMissionContainer.addMission(scoutMission, TransportUnitRequestMission(scout, otherIslandDestination))
    }

    private fun createMissionFromUnusedUnits(
        player: Player,
        playerMissionContainer: PlayerMissionsContainer
    ) {
        for (unit in player.units) {
            if (unit.unitRole.equalsId(UnitRole.SCOUT) && !playerMissionContainer.isUnitBlockedForMission(unit)) {
                val scoutDestination = findScoutDestination(unit)
                if (scoutDestination is ScoutDestination.Lack) {
                    // no destination so worker unit planer should handle it
                    continue
                }

                val scoutMission = ScoutMission(unit)
                playerMissionContainer.addMission(scoutMission)

                if (scoutDestination is ScoutDestination.OtherIslandFromCarrier) {
                    scoutMission.waitForTransport(scoutDestination.tile)
                    playerMissionContainer.addMission(scoutMission, TransportUnitRequestMission(scoutMission.scout, scoutDestination.tile))
                }
                // else ScoutMissionHandler should take care about mission and tile destination
            }
        }
    }

    private fun canAffordForScout(player: Player): Boolean {
        val scoutRole = Specification.instance.unitRoles.getById(UnitRole.SCOUT)
        var goldSum = 0
        for (requiredGood in scoutRole.requiredGoods) {
            goldSum += player.market().getBidPrice(requiredGood.goodsType, requiredGood.amount)
        }
        val scoutType = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST)
        goldSum += player.europe.aiUnitPrice(scoutType)
        return player.hasGold(goldSum)
    }

    private fun buyScoutInEurope(player: Player): Unit {
        val colonist = player.europe.buyUnitByAI(Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST))
        val scoutRole = Specification.instance.unitRoles.getById(UnitRole.SCOUT)
        player.europe.changeUnitRole(game, colonist, scoutRole)
        return colonist
    }
}