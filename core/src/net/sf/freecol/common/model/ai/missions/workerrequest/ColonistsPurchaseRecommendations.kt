package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.workerrequest.WorkerRequestLogger.*
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.Units
import promitech.colonization.ai.score.ScoreableObjectsList
import kotlin.math.log

class ColonistsPurchaseRecommendations(
    val player: Player,
    val playerMissionContainer: PlayerMissionsContainer
) {

    fun buyRecommendations(
        workerPlaceCalculator: ColonyWorkerRequestPlaceCalculator,
        entryPointTurnRange: EntryPointTurnRange
    ) {
        val recommendations = generateRecommendations(workerPlaceCalculator, entryPointTurnRange)
        for (buyRecommendation in recommendations) {
            if (player.europe.canAiBuyUnit(buyRecommendation.workerType(), player.gold)) {
                val newUnit = player.europe.buyUnitByAI(buyRecommendation.workerType())
                val mission = ColonyWorkerMission(buyRecommendation.location, newUnit, buyRecommendation.goodsType)
                playerMissionContainer.addMission(mission)
            }
        }
    }

    fun generateRecommendations(
        workerPlaceCalculator: ColonyWorkerRequestPlaceCalculator,
        entryPointTurnRange: EntryPointTurnRange
    ): ScoreableObjectsList<WorkerRequestScoreValue> {
        val tileScore = workerPlaceCalculator.score(playerMissionContainer)

        val scorePolicy = ScorePolicy.WorkerPriceToValue(entryPointTurnRange, player)
        scorePolicy.calculateScore(tileScore)

        val navyCapacity = Units.calculateNavyCapacity(player) - occupiedNavyCapacity()

        val buyRecomendations = createList(player.gold, navyCapacity, tileScore)
        return buyRecomendations
    }

    private fun occupiedNavyCapacity(): Int {
        val missions = playerMissionContainer.findMissions(ColonyWorkerMission::class.java)
        return missions.size
    }

    private fun createList(
        gold: Int, navyCargoCapacity: Int, workerRequests: ScoreableObjectsList<WorkerRequestScoreValue>
    ): ScoreableObjectsList<WorkerRequestScoreValue> {
        if (navyCargoCapacity == 0) {
            logger.debug("PurchaseColonists.player[%s] no navy cargo capacity", player.id)
            return ScoreableObjectsList<WorkerRequestScoreValue>(0)
        }
        var budget = gold
        val list = ScoreableObjectsList<WorkerRequestScoreValue>(navyCargoCapacity)
        for (workerRequest in workerRequests) {
            if (list.size() >= navyCargoCapacity) {
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