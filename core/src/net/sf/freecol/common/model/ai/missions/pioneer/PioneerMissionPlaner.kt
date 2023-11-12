package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.ColonySupplyGoods
import net.sf.freecol.common.model.ai.missions.MissionId
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.foreachMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.colonyproduction.GoodsCollection
import net.sf.freecol.common.model.colonyproduction.amount
import net.sf.freecol.common.model.colonyproduction.type
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.score.ObjectScoreList
import java.util.HashSet

sealed class PioneerDestination {
    class TheSameIsland(val plan: ColonyTilesImprovementPlan) : PioneerDestination()
    class OtherIsland(val plan: ColonyTilesImprovementPlan) : PioneerDestination()
    class Lack : PioneerDestination()

    fun extractColonyDestination(): Colony? {
        return when (this) {
            is TheSameIsland -> plan.colony
            is OtherIsland -> plan.colony
            is Lack -> null
        }
    }
}

data class PioneerBuyPlan(val buyPioneerOrder: BuyPioneerOrder, val colony: Colony)

data class ColonyHardyPioneer(val colony: Colony, val hardyPioneer: Unit)


/**
 * Running up that hill.
 */
class PioneerMissionPlaner(val game: Game, val pathFinder: PathFinder) {
    private val pioneerSupplyLandRange = 3
    private val pioneerSupplySeaRange = 2
    private val minDistanceToUseShipTransport = 5
    private val improvedAllTilesInColonyToAllColonyRatio: Double = 0.7

    fun createMissionFromUnusedUnit(unit: Unit, playerMissionContainer: PlayerMissionsContainer): PioneerMission? {
        if (!isPioneerUnit(unit)) {
            return null
        }

        val pioneerDestination = findNextColonyToImprove(unit, null, playerMissionContainer)
        when (pioneerDestination) {
            is PioneerDestination.TheSameIsland -> {
                val pioneerMission = PioneerMission(unit, pioneerDestination.plan.colony)
                playerMissionContainer.addMission(pioneerMission)
                return pioneerMission
            }
            is PioneerDestination.OtherIsland -> {
                val pioneerMission = PioneerMission(unit, pioneerDestination.plan.colony)
                playerMissionContainer.addMission(pioneerMission)
                val transportUnitRequestMission = TransportUnitRequestMission(game.turn, unit, pioneerDestination.plan.colony.tile)
                    .withCheckAvailability()
                playerMissionContainer.addMission(pioneerMission, transportUnitRequestMission)
                return pioneerMission
            }
            is PioneerDestination.Lack -> {
                return null
            }
        }
    }

    private fun isPioneerUnit(unit: Unit): Boolean {
        return unit.unitRole.equalsId(UnitRole.PIONEER) && unit.roleCount > 0
    }

    fun createBuyPlan(player: Player, playerMissionContainer: PlayerMissionsContainer): PioneerBuyPlan? {
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
            return null
        }
        // simplification: existed pioneer units without mission should handled by default colony worker

        val firstDestination: ColonyTilesImprovementPlan? = firstImprovementColonyDestination(improvementsPlanDestinationsScore, colonyWithMissions)
        if (firstDestination == null) {
            return null
        }

        val colonyHardyPioneerInRange = findColonyHardyPioneerInRange(player, firstDestination.colony.tile)
        val colonyToEquiptPioneer = findColonyToEquiptPioneerInRange(player, firstDestination.colony.tile, null)

        val buyPioneerOrder = calculateBuyPioneerOrder(player, hasSpecialistOnMission, colonyHardyPioneerInRange, colonyToEquiptPioneer)
        if (buyPioneerOrder is BuyPioneerOrder.CanNotAfford) {
            return null
        }
        return PioneerBuyPlan(buyPioneerOrder, firstDestination.colony)
    }

    fun findColonyHardyPioneerInRange(
        player: Player,
        sourceTile: Tile
    ): ColonyHardyPioneer? {
        val playerMissionContainer = game.aiContainer.missionContainer(player)

        val freeColonistPathUnit = pathFinder.createPathUnit(player, Specification.instance.freeColonistUnitType)
        pathFinder.generateRangeMap(game.map, sourceTile, freeColonistPathUnit, PathFinder.includeUnexploredTiles, pioneerSupplyLandRange)

        val colonyHardyPioneer = findHardyPioneerInPathFindRange(playerMissionContainer, pathFinder, player)
        if (colonyHardyPioneer != null) {
            return colonyHardyPioneer
        }
        val pathUnit = pathFinder.createPathUnit(player, Specification.instance.unitTypes.getById(UnitType.CARAVEL))
        pathFinder.generateRangeMap(game.map, sourceTile, pathUnit, PathFinder.includeUnexploredTiles, pioneerSupplySeaRange)
        return findHardyPioneerInPathFindRange(playerMissionContainer, pathFinder, player)
    }

    private fun findHardyPioneerInPathFindRange(
        playerMissionContainer: PlayerMissionsContainer,
        rangePathFinder: PathFinder,
        player: Player
    ): ColonyHardyPioneer? {
        var minDistance = Int.MAX_VALUE
        var minDistanceColony: Colony? = null
        var colonyHardyPioneer: Unit? = null

        for (settlement in player.settlements) {
            val distance = rangePathFinder.turnsCost(settlement.tile)
            if (distance < minDistance) {
                val colony = settlement.asColony()
                val pioneer = findHardyPioneer(colony, playerMissionContainer)
                if (pioneer != null) {
                    minDistance = distance
                    minDistanceColony = colony
                    colonyHardyPioneer = pioneer
                }
            }
        }
        if (colonyHardyPioneer != null && minDistanceColony != null) {
            return ColonyHardyPioneer(minDistanceColony, colonyHardyPioneer)
        }
        return null
    }

    private fun findHardyPioneer(colony: Colony, playerMissionContainer: PlayerMissionsContainer): Unit? {
        for (unit in colony.units.entities()) {
            if (unit.unitType.equalsId(UnitType.HARDY_PIONEER) && !playerMissionContainer.isUnitBlockedForMission(unit)) {
                return unit
            }
        }
        return null
    }

    fun findColonyToEquiptPioneerInRange(player: Player, sourceTile: Tile, pioneerMissionId: MissionId?): Colony? {
        val playerAiContainer = game.aiContainer.playerAiContainer(player)

        val freeColonistPathUnit = pathFinder.createPathUnit(player, Specification.instance.freeColonistUnitType)
        pathFinder.generateRangeMap(game.map, sourceTile, freeColonistPathUnit, PathFinder.includeUnexploredTiles, pioneerSupplyLandRange)

        val pioneerRole = Specification.instance.unitRoles.getById(UnitRole.PIONEER)
        val pioneerRoleRequiredGoods = pioneerRole.sumOfRequiredGoods()

        val hasColonyRequiredGoodsPredicate: (Colony) -> Boolean = { colony ->
            hasColonyRequiredGoods(colony, pioneerRoleRequiredGoods, playerAiContainer.findColonySupplyGoods(colony), pioneerMissionId)
        }

        val colonyWithToolsSupply = findToolsSupplyInPathRange(pathFinder, player, hasColonyRequiredGoodsPredicate)
        if (colonyWithToolsSupply != null) {
            return colonyWithToolsSupply
        }
        val pathUnit = pathFinder.createPathUnit(player, Specification.instance.unitTypes.getById(UnitType.CARAVEL))
        pathFinder.generateRangeMap(game.map, sourceTile, pathUnit, PathFinder.includeUnexploredTiles, pioneerSupplySeaRange)
        return findToolsSupplyInPathRange(pathFinder, player, hasColonyRequiredGoodsPredicate)
    }

    fun hasColonyRequiredGoods(
        colony: Colony,
        requiredGoods: GoodsCollection,
        colonySupplyGoods: ColonySupplyGoods?,
        pioneerMissionId: MissionId?
    ): Boolean {
        for (requiredGood in requiredGoods) {
            val reservationSum = if (colonySupplyGoods != null) colonySupplyGoods.reservationSum(requiredGood.type(), pioneerMissionId) else 0
            if (colony.goodsContainer.goodsAmount(requiredGood.type()) < requiredGood.amount() + reservationSum) {
                return false
            }
        }
        return true
    }

    private fun findToolsSupplyInPathRange(
        rangePathFinder: PathFinder,
        player: Player,
        colonyHasRequiredGoodsPredicate: (colony: Colony) -> Boolean
    ): Colony? {
        var minDistance = Int.MAX_VALUE
        var minDistanceColony: Colony? = null

        for (settlement in player.settlements) {
            val distance = rangePathFinder.turnsCost(settlement.tile)
            if (distance < minDistance) {
                val colony = settlement.asColony()
                if (colonyHasRequiredGoodsPredicate(colony)) {
                    minDistance = distance
                    minDistanceColony = colony
                }
            }
        }
        return minDistanceColony
    }

    fun handlePioneerBuyPlan(pioneerBuyPlan: PioneerBuyPlan, playerMissionContainer: PlayerMissionsContainer) {
        pioneerBuyPlan.buyPioneerOrder.buyAndPrepareMissions(playerMissionContainer, pioneerBuyPlan.colony, game)
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
            return findNextColonyToImprove(
                mission.pioneer,
                mission.colonyId,
                playerMissionContainer
            )
        }
    }

    fun findNextColonyToImprove(pioneer: Unit, actualPioneerColonyId: String?, playerMissionContainer: PlayerMissionsContainer): PioneerDestination {
        val colonyWithMissions = HashSet<String>(playerMissionContainer.player.settlements.size())
        playerMissionContainer.foreachMission(PioneerMission::class.java) { pioneerMission ->
            colonyWithMissions.add(pioneerMission.colonyId)
        }

        val improvementsPlanDestinationsScore = generateImprovementsPlanScore(playerMissionContainer.player, AddImprovementPolicy.Balanced())
        pathFinder.generateRangeMap(
            game.map,
            pioneer.positionRelativeToMap(game.map),
            pioneer,
            PathFinder.includeUnexploredTiles
        )

        val improvementDestination: ColonyTilesImprovementPlan? = findTheBestDestinationInRange(
            pathFinder,
            improvementsPlanDestinationsScore,
            colonyWithMissions,
            actualPioneerColonyId
        )
        if (improvementDestination == null || improvementDestination.isEmpty()) {
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
        colonyWithMissions: Set<String>,
        actualPioneerColonyId: String?
    ): ColonyTilesImprovementPlan? {
        var improvementDestination: ColonyTilesImprovementPlan? = null
        var destinationScore: Double = 0.0
        for (planScore in improvementsPlanDestinationsScore) {
            if (actualPioneerColonyId != null && planScore.obj.colony.equalsId(actualPioneerColonyId)) {
                improvementDestination = planScore.obj
                destinationScore = planScore.score().toDouble() / nextColonyDistanceValue(rangePathFinder, planScore.obj.colony)
            }
        }
        for (planScore in improvementsPlanDestinationsScore) {
            if (planScore.score() == 0 || colonyWithMissions.contains(planScore.obj.colony.id)) {
                continue
            }
            val dscore: Double = planScore.score().toDouble() / nextColonyDistanceValue(rangePathFinder, planScore.obj.colony)
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

    private fun firstImprovementColonyDestination(
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

    fun calculateBuyPioneerOrder(
        player: Player,
        hasSpecialistOnMission: Boolean,
        colonyHardyPioneerInRange: ColonyHardyPioneer? = null,
        colonyToEquiptPioneer: Colony? = null
    ): BuyPioneerOrder {
        val freeColonistPrice = player.europe.aiUnitPrice(Specification.instance.freeColonistUnitType)
        val pioneerRole = Specification.instance.unitRoles.getById(UnitRole.PIONEER)

        if (colonyHardyPioneerInRange != null) {
            if (colonyToEquiptPioneer != null) {
                if (player.hasGold(freeColonistPrice * 2)) {
                    return BuyPioneerOrder.RecruitColonistWithToolsAndHardyPionnerLocation(colonyToEquiptPioneer, colonyHardyPioneerInRange)
                }
                return BuyPioneerOrder.CanNotAfford()
            } else {
                val pioneerPrice = player.europe.aiUnitPrice(Specification.instance.freeColonistUnitType, pioneerRole)
                if (player.hasGold(pioneerPrice * 2)) {
                    return BuyPioneerOrder.RecruitColonistWithHardyPioneerLocation(colonyHardyPioneerInRange)
                }
                return BuyPioneerOrder.CanNotAfford()
            }
        }

        val hardyPioneerUnitType = Specification.instance.unitTypes.getById(UnitType.HARDY_PIONEER)
        val hardyPioneerPrice = player.europe.aiUnitPrice(hardyPioneerUnitType)

        if (player.hasGold(hardyPioneerPrice * 2)) {
            return BuyPioneerOrder.BuySpecialistOrder()
        } else if (!hasSpecialistOnMission) {
            if (colonyToEquiptPioneer != null) {
                if (player.hasGold(freeColonistPrice * 2)) {
                    return BuyPioneerOrder.RecruitColonistWithToolsLocation(colonyToEquiptPioneer)
                }
            } else {
                val pioneerPrice = player.europe.aiUnitPrice(Specification.instance.freeColonistUnitType, pioneerRole)
                if (player.hasGold(pioneerPrice * 2)) {
                    return BuyPioneerOrder.RecruitColonistOrder()
                }
            }
        }
        return BuyPioneerOrder.CanNotAfford()
    }

    fun generateImprovementsPlanScore(player: Player, policy: AddImprovementPolicy): ObjectScoreList<ColonyTilesImprovementPlan> {
        val improvementPlanScore = ObjectScoreList<ColonyTilesImprovementPlan>(player.settlements.size())
        for (settlement in player.settlements) {
            val colony = settlement.asColony()
            val improvementPlan = policy.generateImprovements(colony)
            improvementPlanScore.add(improvementPlan, calculateScore(improvementPlan))
        }

        // ratio of colonises without improvements to colonies with improvements
        val actualRatio = calculateColoniesWithoutImprovementsToAllColoniesRatio(player, improvementPlanScore)
        if (actualRatio >= improvedAllTilesInColonyToAllColonyRatio) {
            for (scorePlan in improvementPlanScore) {
                val plan = scorePlan.obj
                policy.generateVacantForFood(plan)
                scorePlan.updateScore(calculateScore(plan))
            }
        }

        improvementPlanScore.sortDescending()
        return improvementPlanScore
    }

    private fun calculateColoniesWithoutImprovementsToAllColoniesRatio(
        player: Player,
        improvementPlanScore: ObjectScoreList<ColonyTilesImprovementPlan>
    ): Double {
        var allImprovementsCount = 0
        for (scorePlan in improvementPlanScore) {
            if (!scorePlan.obj.hasImprovements()) {
                allImprovementsCount++
            }
        }
        return allImprovementsCount.toDouble() / player.settlements.size()
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