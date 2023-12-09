package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.util.whenNotNull
import promitech.colonization.ai.score.ScoreableObjectsList
import promitech.colonization.screen.debug.TileDebugView

class ColonyWorkerRequestPlaceCalculator(
    val player: Player,
    val map: Map,
    val entryPointTurnRange: EntryPointTurnRange,
    val pathFinder: PathFinder
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
        tileScore.clear()
        if (player.settlements.isEmpty()) {
            workerForCreateColony(player, tileScore, playerMissionsContainer);
            // tileScore already sorted
        } else {
            workerForColony(player, tileScore)
            if (allowBuildNewColonies()) {
                workerForCreateColony(player, tileScore, playerMissionsContainer);
            }
        }
        workerPriceToValueScorePolicy.calculateScore(tileScore)
        tileScore.sortDescending()
        return tileScore
    }

    private fun workerForCreateColony(
        player: Player,
        tileScore: ScoreableObjectsList<WorkerRequestScoreValue>,
        playerMissionsContainer: PlayerMissionsContainer
    ) {
        val placeGenerator = TheBestColonyPlaceGenerator(map, pathFinder)
        val theBestTiles = placeGenerator.theBestTiles(player, playerMissionsContainer)
        theBestTiles.firstTheBest().whenNotNull { tile ->
            val newColonyReqScore = CreateColonyReqScore(map, player, goodsTypeToScoreByPrice)
            newColonyReqScore.score(tileScore, tile)
        }
        tileScore.sortDescending()
    }

    private fun workerForColony(player: Player, tileScore: ScoreableObjectsList<WorkerRequestScoreValue>) {
        for (settlement in player.settlements) {
            colonyWorkerReq.simulate(settlement.asColony(), tileScore)
        }
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

    fun debugTheBestPlaceToBuildColony(tileDebugView: TileDebugView, playerMissionsContainer: PlayerMissionsContainer) {
        val colonyPlaceGenerator = TheBestColonyPlaceGenerator(map, pathFinder)
        val theBestTiles = colonyPlaceGenerator.theBestTiles(player, playerMissionsContainer);

        println("the best place for colony, maxTurnsRange: ${theBestTiles.maxTurns}")
        for (turn in 0 .. theBestTiles.maxTurns) {
            val tilesInTurns = theBestTiles.tilesInTurns(turn)
            println("turn[$turn] tiles ${tilesInTurns.size}")

            for (i in 0 until tilesInTurns.size) {
                println("turn[$turn] w: ${tilesInTurns[i].weight} tile: ${tilesInTurns[i].tile}")
                tileDebugView.str(tilesInTurns[i].tile, "t${turn} ${tilesInTurns[i].weight}")
            }
        }
    }
}