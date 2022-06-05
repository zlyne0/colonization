package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.hasMissionKt
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.MissionHandlerLogger
import promitech.colonization.ai.Units
import promitech.colonization.ai.score.ScoreableObjectsList

class ColonyWorkerRequestPlaner(
    private val player: Player,
    private val playerMissionsContainer: PlayerMissionsContainer,
    private val game: Game,
    pathFinder: PathFinder
) {

    private val transporter: Unit?
    private var entryPointTurnRange: EntryPointTurnRange
    private var placeCalculator: ColonyWorkerRequestPlaceCalculator

    init {
        transporter = Units.findCarrier(player)
        if (transporter == null) {
            MissionHandlerLogger.logger.debug("player[%s] no carrier unit", player.id)
        }

        entryPointTurnRange = EntryPointTurnRange(game.map, pathFinder, player, transporter)
        placeCalculator = ColonyWorkerRequestPlaceCalculator(
            player,
            game.map,
            entryPointTurnRange
        )
    }

    fun createBuyPlan() {
        if (transporter == null) {
            return
        }
        val purchaseRecommendations = ColonistsPurchaseRecommendations(player, playerMissionsContainer)
        purchaseRecommendations.buyRecommendations(placeCalculator, entryPointTurnRange)
    }

    fun createMissionFromUnusedUnits() {
        for (unit in player.units) {
            if (unit.isAtTileLocation) {
                if (playerMissionsContainer.isUnitBlockedForMission(unit)) {
                    continue
                }
                val tileScore = placeCalculator.score(playerMissionsContainer)

                findTheBestLocationDependsTransporter(unit, tileScore)?.let { place ->
                    val mission = ColonyWorkerMission(place.location(), unit, place.goodsType())
                    playerMissionsContainer.addMission(mission)
                }
            } else if (unit.isAtEuropeLocation || unit.isAtUnitLocation) {
                if (playerMissionsContainer.isUnitBlockedForMission(unit)) {
                    continue
                }
                if (Unit.isColonist(unit.unitType, unit.owner)) {
                    val tileScore = placeCalculator.score(playerMissionsContainer)
                    findTheBestLocationOnMap(unit, tileScore)?.let { place ->
                        val mission = ColonyWorkerMission(place.location(), unit, place.goodsType())
                        playerMissionsContainer.addMission(mission)
                    }
                }
            }
        }
    }

    private fun findTheBestLocationDependsTransporter(unit: Unit, tileScore: ScoreableObjectsList<WorkerRequestScoreValue>): WorkerRequestScoreValue? {
        val place = findTheBestLocationOnTheSameIsland(unit, tileScore)
        if (place != null) {
            return place
        }
        if (transporter == null) {
            return null
        }
        return findTheBestLocation(unit, tileScore, { _ -> true})
    }

    private fun findTheBestLocationOnTheSameIsland(unit: Unit, tileScore: ScoreableObjectsList<WorkerRequestScoreValue>): WorkerRequestScoreValue? {
        val unitTile = unit.tile
        val place = findTheBestLocation(unit, tileScore, { workerRequestScoreValue ->
            game.map.isTheSameArea(unitTile, workerRequestScoreValue.location)
        })
        return place
    }

    private fun findTheBestLocationOnMap(unit: Unit, tileScore: ScoreableObjectsList<WorkerRequestScoreValue>): WorkerRequestScoreValue? {
        return findTheBestLocation(unit, tileScore, { _ -> true})
    }

    private inline fun findTheBestLocation(
        unit: Unit,
        tileScore: ScoreableObjectsList<WorkerRequestScoreValue>,
        pred: (wrsv: WorkerRequestScoreValue) -> Boolean
    ): WorkerRequestScoreValue? {
        var unitPlace: WorkerRequestScoreValue? = null
        var freeColonistsPlace: WorkerRequestScoreValue? = null

        // tileScore: the best tiles are on begining list

        for (workerRequestScore in tileScore) {
            if (!hasMissionToWorkerRequestPlace(workerRequestScore)) {
                if (unitPlace == null && pred.invoke(workerRequestScore) && unit.unitType.equalsId(workerRequestScore.workerType())) {
                    unitPlace = workerRequestScore
                }
                if (freeColonistsPlace == null && pred.invoke(workerRequestScore) && workerRequestScore.workerType().equalsId(UnitType.FREE_COLONIST)) {
                    freeColonistsPlace = workerRequestScore
                }
            }
        }
        var place = unitPlace
        if (place == null) {
            place = freeColonistsPlace
        }
        return place
    }

    private fun hasMissionToWorkerRequestPlace(workerRequestScore: WorkerRequestScoreValue): Boolean {
        return playerMissionsContainer.hasMissionKt(ColonyWorkerMission::class.java, { mission ->
            mission.getTile().equalsCoordinates(workerRequestScore.location)
                && mission.getGoodsType().equalsId(workerRequestScore.goodsType)
        })
    }
}