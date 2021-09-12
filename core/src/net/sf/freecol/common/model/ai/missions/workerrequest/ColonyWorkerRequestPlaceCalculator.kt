package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.MapIdEntities
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.GoodsType
import promitech.colonization.ai.score.ScoreableObjectsList
import promitech.colonization.screen.debug.TileDebugView

class ColonyWorkerRequestPlaceCalculator(
    val player : Player,
    val map: Map,
    val entryPointTurnRange: EntryPointTurnRange
) {

//	1col +0
//	2col +5
//	3col +7
//	4col +10
//	5col +12
//	6col +15
//    int [][] colNumberWeights = new int[][] {
//        {0, 0},
//        {1, 0},
//        {2, 5},
//        {3, 7},
//        {4, 10},
//        {5, 12},
//        {6, 15}
//    };

    private val tileScore = ScoreableObjectsList<WorkerRequestScoreValue>(20)
    private val goodsTypeToScoreByPrice = Specification.instance.goodsTypeToScoreByPrice
    private val colonyWorkerReq = ColonyWorkerReqScore(player.market(), goodsTypeToScoreByPrice)

    fun score(): ScoreableObjectsList<WorkerRequestScoreValue> {
        tileScore.clear()
        if (player.settlements.isEmpty()) {
            workerForCreateColony(player, tileScore)
            // tileScore already sorted
        } else {
            workerForColony(player, tileScore)
            workerForCreateColony(player, tileScore)
        }
        tileScore.sortDescending()
        return tileScore
    }

    private fun workerForCreateColony(player: Player, tileScore: ScoreableObjectsList<WorkerRequestScoreValue>) {
        val colonyPlaceGenerator = ColonyPlaceGenerator(entryPointTurnRange, map)
        val theBestTiles = colonyPlaceGenerator.theBestTiles(player)
        val newColonyReqScore = CreateColonyReqScore(map, player, goodsTypeToScoreByPrice)
        newColonyReqScore.score(tileScore, theBestTiles)
        tileScore.sortDescending()
    }

    private fun workerForColony(player: Player, tileScore: ScoreableObjectsList<WorkerRequestScoreValue>) {
        for (settlement in player.settlements) {
            colonyWorkerReq.simulate(settlement.asColony(), tileScore)
        }
    }

    fun debugToConsole(player: Player) {
        printTileScores(player, tileScore)
    }

    fun debug(mapDebugInfo: MapTileDebugInfo) {
        printTileScores(player, tileScore)
        for (ts in tileScore) {
            mapDebugInfo.strIfNull(ts.location().x, ts.location().y, "" + ts.score)
        }
    }

    private fun printTileScores(player: Player, tileScore: ScoreableObjectsList<WorkerRequestScoreValue>) {
        for (objectScore in tileScore) {
            println(objectScore.toPrettyString(player, entryPointTurnRange))
        }
    }

    fun debugTheBestBlaceToBuildColony(tileDebugView: TileDebugView) {
        val colonyPlaceGenerator = ColonyPlaceGenerator(entryPointTurnRange, map)
        colonyPlaceGenerator.theBestTiles(player)
        colonyPlaceGenerator.theBestTilesWeight(tileDebugView)
    }

    fun debugGenerateTileScoresForNewColony(tileDebugView: TileDebugView) {
        val colonyPlaceGenerator = ColonyPlaceGenerator(entryPointTurnRange, map)
        colonyPlaceGenerator.generateWeights(player)
        colonyPlaceGenerator.tilesWeights(tileDebugView)
    }

}