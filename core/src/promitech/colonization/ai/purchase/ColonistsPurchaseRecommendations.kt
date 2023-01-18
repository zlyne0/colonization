package promitech.colonization.ai.purchase

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMission
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaceCalculator
import net.sf.freecol.common.model.ai.missions.workerrequest.EntryPointTurnRange
import net.sf.freecol.common.model.ai.missions.workerrequest.ScorePolicy
import net.sf.freecol.common.model.ai.missions.workerrequest.WorkerRequestLogger.logger
import net.sf.freecol.common.model.ai.missions.workerrequest.WorkerRequestScoreValue
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.score.ScoreableObjectsList

class ColonistsPurchaseRecommendations(
    val game: Game,
    val player: Player,
    val playerMissionContainer: PlayerMissionsContainer,
    val entryPointTurnRange: EntryPointTurnRange,
    val workerPlaceCalculator: ColonyWorkerRequestPlaceCalculator
) {

    fun buyRecommendations(workersNumber: Int) {
        val recommendations = generateRecommendations(workersNumber)
        for (buyRecommendation in recommendations) {
            if (player.europe.canAiBuyUnit(buyRecommendation.workerType(), player.gold)) {
                val newUnit = player.europe.buyUnitByAI(buyRecommendation.workerType())

                val mission = ColonyWorkerMission(buyRecommendation.location, newUnit, buyRecommendation.goodsType)
                playerMissionContainer.addMission(mission)
                val transportUnitRequestMission = TransportUnitRequestMission(game.turn, mission.unit, mission.tile)
                playerMissionContainer.addMission(mission, transportUnitRequestMission)
            }
        }
    }

    fun generateRecommendations(workersNumber: Int): ScoreableObjectsList<WorkerRequestScoreValue> {
        val tileScore = workerPlaceCalculator.score(playerMissionContainer)
        val scorePolicy = ScorePolicy.WorkerPriceToValue(entryPointTurnRange, player)
        scorePolicy.calculateScore(tileScore)
        val buyRecomendations = createList(player.gold, workersNumber, tileScore)
        return buyRecomendations
    }

    private fun createList(
        gold: Int, workerNumber: Int, workerRequests: ScoreableObjectsList<WorkerRequestScoreValue>
    ): ScoreableObjectsList<WorkerRequestScoreValue> {
        if (workerNumber <= 0) {
            logger.debug("player[%s].PurchaseColonists no navy cargo capacity", player.id)
            return ScoreableObjectsList<WorkerRequestScoreValue>(0)
        }
        var budget = gold
        val list = ScoreableObjectsList<WorkerRequestScoreValue>(workerNumber)
        for (workerRequest in workerRequests) {
            if (list.size() >= workerNumber) {
                break
            }
            if (!player.europe.isUnitAvailableToBuyByAI(workerRequest.workerType())) {
                logger.debug("PurchaseColonists.unitType[%s] not available to buy in europe", workerRequest.workerType())
                continue
            }
            val unitPrice = player.europe.aiUnitPrice(workerRequest.workerType())
            if (unitPrice > budget) {
                if (logger.isDebug()) {
                    logger.debug("PurchaseColonists.unitType[%s].price[%s] not afforable for player[%s]",
                        workerRequest.workerType(), unitPrice, player.id
                    )
                }
                continue
            }
            budget -= unitPrice
            list.add(workerRequest)
        }
        return list
    }

    fun printToLog(workers: ScoreableObjectsList<WorkerRequestScoreValue>, entryPointTurnRange: EntryPointTurnRange) {
        if (logger.isDebug) {
            var str = "worker recomendations size: ${workers.size()}\n"
            for (worker in workers) {
                str += worker.toPrettyString(player, entryPointTurnRange) + "\n"
            }
            logger.debug(str)
        }
    }

    fun printToMap(buyRecomendations: ScoreableObjectsList<WorkerRequestScoreValue>, mapDebugView: MapTileDebugInfo) {
        for (worker in buyRecomendations) {
            mapDebugView.strIfNull(worker.location.x, worker.location.y, worker.score.toString())
        }
    }

}