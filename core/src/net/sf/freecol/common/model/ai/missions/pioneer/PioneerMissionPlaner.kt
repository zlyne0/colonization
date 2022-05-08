package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.foreachMission
import net.sf.freecol.common.model.colonyproduction.GoodsCollection
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import promitech.colonization.ai.score.ObjectScoreList
import java.util.HashSet

sealed class PioneerDestination {
    class TheSameIsland(val plan: ColonyTilesImprovementPlan) : PioneerDestination()
    class OtherIsland(val plan: ColonyTilesImprovementPlan) : PioneerDestination()
    class Lack : PioneerDestination()
}

class PioneerMissionPlaner(val game: Game, val pathFinder: PathFinder) {
    private val minDistanceToUseShipTransport = 5

    fun prepareMission(player: Player, playerMissionContainer: PlayerMissionsContainer) {
        val colonyWithMissions = HashSet<String>(playerMissionContainer.player.settlements.size())
        var hasSpecialistOnMission = false
        playerMissionContainer.foreachMission(PioneerMission::class.java, { pioneerMission ->
            colonyWithMissions.add(pioneerMission.colonyId)
            if (pioneerMission.isSpecialist()) {
                hasSpecialistOnMission = true
            }
        })
        val improvementsPlanDestinationsScore = generateImprovementsPlanScore(player, AddImprovementPolicy.Balanced())

        if (isReachMaxPioneerMissions(improvementsPlanDestinationsScore, colonyWithMissions)) {
            // no missions
            return
        }

        val firstDestination : ColonyTilesImprovementPlan? = firstImprovmentColonyDestination(improvementsPlanDestinationsScore, colonyWithMissions)
        if (firstDestination != null) {
            val buyPionnierOrder = calculateBuyPioneerOrder(player, hasSpecialistOnMission)
            val boughtPioneer : Unit = when (buyPionnierOrder) {
                is BuyPioneerOrder.BuySpecialistOrder -> buyPionnierOrder.buy(player)
                is BuyPioneerOrder.RecruitColonistOrder -> buyPionnierOrder.buy(player, game)
                else -> return
            }
            val pioneerMission = PioneerMission(boughtPioneer, firstDestination.colony)
            playerMissionContainer.addMission(pioneerMission)
            playerMissionContainer.addMission(pioneerMission, TransportUnitRequestMission(boughtPioneer, firstDestination.colony.tile))
        }
    }

    private fun isReachMaxPioneerMissions(
        improvementsPlanDestinationsScore: ObjectScoreList<ColonyTilesImprovementPlan>,
        colonyWithMissions: Set<String>
    ): Boolean {
        var potentialImprovementsDestinationCount = 0
        for (objectScore in improvementsPlanDestinationsScore) {
            if (objectScore.obj.hasImprovements()) {
                potentialImprovementsDestinationCount++
            }
        }
        // no more new pioneers mission then half of improvements destinations
        val maxPioneersCount: Int = potentialImprovementsDestinationCount / 2
        if (colonyWithMissions.size >= maxPioneersCount) {
            // no missions
            return true
        }
        return false
    }

    fun findImprovementDestination(mission: PioneerMission, playerMissionContainer: PlayerMissionsContainer): PioneerDestination {
        val improvementsPlan = generateImprovementsPlanForColony(mission.pioneer.owner, mission.colonyId)
        if (improvementsPlan.hasImprovements()) {
            val firstImprovement = improvementsPlan.firstImprovement()

            pathFinder.findToTile(game.map, mission.pioneer, firstImprovement.tile, PathFinder.includeUnexploredTiles)
            if (pathFinder.isTurnCostAbove(firstImprovement.tile, minDistanceToUseShipTransport)) {
                return PioneerDestination.OtherIsland(improvementsPlan)
            }
            return PioneerDestination.TheSameIsland(improvementsPlan)
        } else {
            return findNextColonyToImprove(mission, playerMissionContainer)
        }
    }

    fun findNextColonyToImprove(mission: PioneerMission, playerMissionContainer: PlayerMissionsContainer): PioneerDestination {
        val colonyWithMissions = HashSet<String>(playerMissionContainer.player.settlements.size())
        playerMissionContainer.foreachMission(PioneerMission::class.java, { pioneerMission ->
            colonyWithMissions.add(pioneerMission.colonyId)
        })

        val improvementsPlanDestinationsScore = generateImprovementsPlanScore(playerMissionContainer.player, AddImprovementPolicy.Balanced())
        pathFinder.generateRangeMap(game.map, mission.pioneer, PathFinder.includeUnexploredTiles)

        var improvementDestination: ColonyTilesImprovementPlan? = findTheBestDestinationInRange(
            pathFinder,
            improvementsPlanDestinationsScore,
            colonyWithMissions
        )
        if (improvementDestination == null) {
            return PioneerDestination.Lack()
        }
        if (pathFinder.isTurnCostAbove(improvementDestination.colony.tile, minDistanceToUseShipTransport)) {
            return PioneerDestination.OtherIsland(improvementDestination)
        }
        return PioneerDestination.TheSameIsland(improvementDestination)
    }

    private fun findTheBestDestinationInRange(
        rangePathFinder: PathFinder,
        improvementsPlanDestinationsScore: ObjectScoreList<ColonyTilesImprovementPlan>,
        colonyWithMissions: Set<String>
    ): ColonyTilesImprovementPlan? {
        var improvementDestination: ColonyTilesImprovementPlan? = null
        var destinationScore: Double = 0.0
        for (planScore in improvementsPlanDestinationsScore) {
            if (planScore.score == 0 || colonyWithMissions.contains(planScore.obj.colony.id)) {
                continue
            }
            val dscore: Double = planScore.score.toDouble() / nextColonyDistanceValue(rangePathFinder, planScore.obj.colony)
            if (improvementDestination == null || dscore > destinationScore) {
                destinationScore = dscore
                improvementDestination = planScore.obj
            }
        }
        return improvementDestination
    }

    private fun nextColonyDistanceValue(rangePathFinder: PathFinder, colony: Colony): Int {
        var distance = rangePathFinder.turnsCost(colony.tile)
        if (distance == 0) {
            distance = 1
        } else {
            if (distance == PathFinder.INFINITY) {
                distance = 10
            }
        }
        return distance
    }

    private fun firstImprovmentColonyDestination(
        improvementsPlanDestinationsScore: ObjectScoreList<ColonyTilesImprovementPlan>,
        colonyWithMissions: Set<String>
    ): ColonyTilesImprovementPlan? {
        for (destinationScore in improvementsPlanDestinationsScore) {
            if (destinationScore.obj.hasImprovements() && !colonyWithMissions.contains(destinationScore.obj.colony.id)) {
                return destinationScore.obj
            }
        }
        return null
    }

    fun calculateBuyPioneerOrder(player: Player, hasSpecialistOnMission: Boolean): BuyPioneerOrder {
        val hardyPioneerUnitType = Specification.instance.unitTypes.getById(UnitType.HARDY_PIONEER)
        val hardyPioneerPrice = player.europe.aiUnitPrice(hardyPioneerUnitType)
        if (player.hasGold(hardyPioneerPrice * 2)) {
            return BuyPioneerOrder.BuySpecialistOrder(hardyPioneerUnitType)
        } else if (!hasSpecialistOnMission) {
            val colonistPrice = player.europe.aiUnitPrice(Specification.instance.freeColonistUnitType)

            val pioneerRole = Specification.instance.unitRoles.getById(UnitRole.PIONEER)
            val pioneerSumOfRequiredGoods : GoodsCollection = pioneerRole.sumOfRequiredGoods()
            val pionnerPrice = player.market().aiBidPrice(pioneerSumOfRequiredGoods) + colonistPrice

            if (player.hasGold(pionnerPrice * 2)) {
                return BuyPioneerOrder.RecruitColonistOrder(pioneerRole)
            }
        }
        return BuyPioneerOrder.CanNotAfford()
    }

    fun generateImprovementsPlanScore(player: Player, policy: AddImprovementPolicy): ObjectScoreList<ColonyTilesImprovementPlan> {
        val objectScore = ObjectScoreList<ColonyTilesImprovementPlan>(player.settlements.size())
        for (settlement in player.settlements) {
            val colony = settlement.asColony()
            val improvementPlan = policy.generateImprovements(colony)
            objectScore.add(improvementPlan, calculateScore(improvementPlan))
        }
        objectScore.sortDescending()
        return objectScore
    }

    fun generateImprovementsPlanForColony(player: Player, colonyId: String): ColonyTilesImprovementPlan {
        val colony = player.settlements.getById(colonyId).asColony()
        val improvementPolicy = AddImprovementPolicy.Balanced()
        return improvementPolicy.generateImprovements(colony)
    }

    private fun calculateScore(improvementPlan: ColonyTilesImprovementPlan): Int {
        if (improvementPlan.improvements.size == 0) {
            return 0
        }
        var resCount = 0
        for (improvement in improvementPlan.improvements) {
            if (improvement.tile.hasTileResource()) {
                resCount++
            }
        }
        return improvementPlan.colony.settlementWorkers().size * 10 + resCount
    }
}