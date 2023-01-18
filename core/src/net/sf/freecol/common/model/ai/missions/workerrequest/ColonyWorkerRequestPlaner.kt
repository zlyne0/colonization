package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.hasMissionKt
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.Units
import promitech.colonization.ai.findCarrier
import promitech.colonization.ai.purchase.ColonistsPurchaseRecommendations
import promitech.colonization.ai.score.ScoreableObjectsList

class ColonyWorkerRequestPlaner(
    private val game: Game,
    private val pathFinder: PathFinder
) {

    private var hasTransporter: Boolean = false
    private lateinit var player: Player
    private lateinit var playerMissionsContainer: PlayerMissionsContainer
    private lateinit var entryPointTurnRange: EntryPointTurnRange
    private lateinit var placeCalculator: ColonyWorkerRequestPlaceCalculator

    private fun init(player: Player, playerMissionsContainer: PlayerMissionsContainer) {
        this.hasTransporter = player.findCarrier() != null
        this.player = player
        this.playerMissionsContainer = playerMissionsContainer

        entryPointTurnRange = EntryPointTurnRange(game.map, pathFinder, player)
        placeCalculator = ColonyWorkerRequestPlaceCalculator(
            player,
            game.map,
            entryPointTurnRange
        )
    }

    fun buyUnitsToNavyCapacity(player: Player, playerMissionContainer: PlayerMissionsContainer, transporter: Unit) {
        init(player, playerMissionContainer)
        val purchaseRecommendations = ColonistsPurchaseRecommendations(game, player, playerMissionContainer, entryPointTurnRange, placeCalculator)
        val workersNumber = Units.transporterCapacity(transporter, playerMissionContainer)
        purchaseRecommendations.buyRecommendations(workersNumber)
    }

    fun createMissionFromUnusedUnits(player: Player, playerMissionsContainer: PlayerMissionsContainer) {
        init(player, playerMissionsContainer)

        for (unit in player.units) {
            if (unit.isNaval || playerMissionsContainer.isUnitBlockedForMission(unit)) {
                continue
            }
            if (unit.isAtTileLocation) {
                val tileScore = placeCalculator.score(playerMissionsContainer)

                findTheBestLocationDependsTransporter(unit, tileScore)?.let { place ->
                    val mission = ColonyWorkerMission(place.location(), unit, place.goodsType())
                    playerMissionsContainer.addMission(mission)
                }
            } else if (unit.isAtEuropeLocation || unit.isAtUnitLocation) {
                if (Unit.isColonist(unit.unitType, unit.owner)) {
                    val tileScore = placeCalculator.score(playerMissionsContainer)
                    findTheBestLocationOnMap(unit, tileScore)?.let { place ->
                        val mission = ColonyWorkerMission(place.location(), unit, place.goodsType())
                        playerMissionsContainer.addMission(mission)

                        val transportUnitRequestMission = TransportUnitRequestMission(game.turn, unit, place.location())
                        playerMissionsContainer.addMission(mission, transportUnitRequestMission)
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
        if (!hasTransporter) {
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