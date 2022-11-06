package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.MapTileDebugInfo
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.foreachMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.ai.missions.workerrequest.WorkerRequestLogger.logger
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.score.ScoreableObjectsList

class ColonistsPurchaseRecommendations(
    val game: Game,
    val player: Player,
    val playerMissionContainer: PlayerMissionsContainer
) {

    fun buyRecommendations(
        workerPlaceCalculator: ColonyWorkerRequestPlaceCalculator,
        entryPointTurnRange: EntryPointTurnRange,
        transporter: Unit
    ) {
        val recommendations = generateRecommendations(workerPlaceCalculator, entryPointTurnRange, transporter)
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

    fun generateRecommendations(
        workerPlaceCalculator: ColonyWorkerRequestPlaceCalculator,
        entryPointTurnRange: EntryPointTurnRange,
        transporter: Unit
    ): ScoreableObjectsList<WorkerRequestScoreValue> {
        val tileScore = workerPlaceCalculator.score(playerMissionContainer)

        val scorePolicy = ScorePolicy.WorkerPriceToValue(entryPointTurnRange, player)
        scorePolicy.calculateScore(tileScore)

        val transporterCapacity = transporterCapacity(transporter)

        val buyRecomendations = createList(player.gold, transporterCapacity, tileScore)
        return buyRecomendations
    }

    private fun transporterCapacity(transporter: Unit): Int {
        var capacity: Int = transporter.freeUnitsSlots()
        playerMissionContainer.foreachMission(TransportUnitMission::class.java, { transportUnitMission ->
            if (transportUnitMission.isCarrier(transporter)) {
                capacity -= transportUnitMission.spaceTakenByUnits()
            }
        })
        return capacity
    }

    private fun createList(
        gold: Int, navyCargoCapacity: Int, workerRequests: ScoreableObjectsList<WorkerRequestScoreValue>
    ): ScoreableObjectsList<WorkerRequestScoreValue> {
        if (navyCargoCapacity <= 0) {
            logger.debug("player[%s].PurchaseColonists no navy cargo capacity", player.id)
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