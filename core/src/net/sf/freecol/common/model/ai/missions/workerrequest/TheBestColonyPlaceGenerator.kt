package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.foreachMission
import net.sf.freecol.common.model.ai.missions.workerrequest.TheBestColonyTilesWeights.TileSelection
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.map.path.TileConsumer
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.util.CollectionUtils

class TheBestColonyPlaceGenerator(
    private val map: Map,
    private val pathFinder: PathFinder
) {

    private val theBestPlacesFromSourceTurnsRange = 2

    private val onlySeasideTileFilter = CollectionUtils.setOf(TileSelection.ONLY_SEASIDE)
    private val caravelUnitType = Specification.instance.unitTypes.getById(UnitType.CARAVEL)

    fun theBestTiles(player: Player, playerMissionsContainer: PlayerMissionsContainer): TheBestColonyTiles {
        val tilesWeights = createTileWeights(player, playerMissionsContainer)
        val theBestColonyTiles = TheBestColonyTiles()

        val tileConsumer = TileConsumer { tile, turns ->
            if (!theBestColonyTiles.isEmpty() && turns > theBestColonyTiles.minTurns + theBestPlacesFromSourceTurnsRange) {
                return@TileConsumer TileConsumer.Status.END
            }
            val tileWeight = tilesWeights.weight(tile)
            if (tileWeight > 0) {
                theBestColonyTiles.add(tile, turns, tileWeight)
            }
            return@TileConsumer TileConsumer.Status.PROCESS
        }

        pathFinder.generateRangeMap(
            map,
            map.getSafeTile(player.entryLocation),
            pathFinder.createPathUnit(player, caravelUnitType),
            PathFinder.includeUnexploredTiles,
            tileConsumer
        )
        return theBestColonyTiles
    }

    private fun createTileWeights(
        player: Player,
        playerMissionsContainer: PlayerMissionsContainer
    ): TheBestColonyTilesWeights {
        val tilesWeights = TheBestColonyTilesWeights(map)
        tilesWeights.generateWeights(player, onlySeasideTileFilter)

        playerMissionsContainer.foreachMission(ColonyWorkerMission::class.java) { mission ->
            tilesWeights.setZeroWeight(mission.tile)
            for (neighbourTile in map.neighbourTiles(mission.tile)) {
                tilesWeights.setZeroWeight(neighbourTile.tile)
            }
        }
        return tilesWeights
    }

}