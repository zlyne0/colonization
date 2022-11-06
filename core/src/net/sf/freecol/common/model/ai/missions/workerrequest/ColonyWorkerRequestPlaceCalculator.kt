package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.score.ScoreableObjectsList
import promitech.colonization.screen.debug.TileDebugView

class ColonyWorkerRequestPlaceCalculator(
    val player: Player,
    val map: Map,
    val entryPointTurnRange: EntryPointTurnRange
) {

    companion object {
        const val smallPoxLimit = 4
        const val colonyRebellionLimit = 50
        const val denseColonyUnitCount = 6
    }

    private val workerPriceToValueScorePolicy = ScorePolicy.WorkerPriceToValue(entryPointTurnRange, player)
    private val tileScore = ScoreableObjectsList<WorkerRequestScoreValue>(20)
    private val goodsTypeToScoreByPrice = Specification.instance.goodsTypeToScoreByPrice
    private val colonyWorkerReq = ColonyWorkerReqScore(player.market(), goodsTypeToScoreByPrice)

    private fun allowBuildNewColonies(): Boolean {
        var rebellionColony = 0
        var denseColony = 0
        for (settlement in player.settlements) {
            val colony = settlement.asColony()
            if (colony.sonsOfLiberty() >= colonyRebellionLimit) {
                rebellionColony++
            }
            if (colony.colonyUnitsCount >= denseColonyUnitCount) {
                denseColony++
            }
        }
        val royalColonies = player.settlements.size() - rebellionColony
        val smallColonies = player.settlements.size() - denseColony
        if (rebellionColony > 0 && royalColonies >= smallPoxLimit || smallColonies >= smallPoxLimit) {
            return false
        }
        return true
    }

    fun score(playerMissionsContainer: PlayerMissionsContainer): ScoreableObjectsList<WorkerRequestScoreValue> {
        val tilesWithCreateColony = tilesForCreateColony(playerMissionsContainer)

        tileScore.clear()
        if (player.settlements.isEmpty()) {
            workerForCreateColony(player, tileScore, tilesWithCreateColony);
            // tileScore already sorted
        } else {
            workerForColony(player, tileScore)
            if (allowBuildNewColonies()) {
                workerForCreateColony(player, tileScore, tilesWithCreateColony);
            }
        }
        workerPriceToValueScorePolicy.calculateScore(tileScore)
        tileScore.sortDescending()
        return tileScore
    }

    private fun tilesForCreateColony(playerMissionsContainer: PlayerMissionsContainer): List<Tile> {
        val missions = playerMissionsContainer.findMissions(ColonyWorkerMission::class.java)
        if (missions.isEmpty()) {
            return emptyList()
        }
        val tiles = mutableListOf<Tile>()
        for (mission in missions) {
            tiles.add(mission.tile)
        }
        return tiles
    }

    private fun workerForCreateColony(player: Player, tileScore: ScoreableObjectsList<WorkerRequestScoreValue>, tilesWithCreateColony: List<Tile>) {
        val colonyPlaceGenerator = ColonyPlaceGenerator(entryPointTurnRange, map)
        val theBestTiles = colonyPlaceGenerator.theBestTiles(player, tilesWithCreateColony)
        val newColonyReqScore = CreateColonyReqScore(map, player, goodsTypeToScoreByPrice)
        newColonyReqScore.score(tileScore, theBestTiles)
        tileScore.sortDescending()
    }

    private fun workerForColony(player: Player, tileScore: ScoreableObjectsList<WorkerRequestScoreValue>) {
        for (settlement in player.settlements) {
            colonyWorkerReq.simulate(settlement.asColony(), tileScore)
        }
    }

    fun debugToConsole() {
        printTileScores(tileScore)
    }

    fun debug(mapDebugInfo: MapTileDebugInfo) {
        printTileScores(tileScore)
        for (ts in tileScore) {
            mapDebugInfo.strIfNull(ts.location().x, ts.location().y, "" + ts.score)
        }
    }

    private fun printTileScores(tileScore: ScoreableObjectsList<WorkerRequestScoreValue>) {
        for (objectScore in tileScore) {
            println(objectScore.toPrettyString(player, entryPointTurnRange))
        }
    }

    fun debugTheBestBlaceToBuildColony(tileDebugView: TileDebugView, tilesWithCreateColonyMission: List<Tile>) {
        val colonyPlaceGenerator = ColonyPlaceGenerator(entryPointTurnRange, map)
        colonyPlaceGenerator.theBestTiles(player, tilesWithCreateColonyMission);
        colonyPlaceGenerator.theBestTilesWeight(tileDebugView)
    }

    fun debugGenerateTileScoresForNewColony(tileDebugView: TileDebugView) {
        val colonyPlaceGenerator = ColonyPlaceGenerator(entryPointTurnRange, map)
        colonyPlaceGenerator.debugTilesWeights(player, tileDebugView)
    }

}