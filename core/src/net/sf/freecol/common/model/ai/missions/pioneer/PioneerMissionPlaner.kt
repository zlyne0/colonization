package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.findMissionAndConsume
import net.sf.freecol.common.model.colonyproduction.GoodsCollection
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.score.ObjectScoreList
import java.util.HashSet

class PioneerMissionPlaner(val game: Game, val pathFinder: PathFinder) {

    fun plan(player: Player, playerMissionContainer: PlayerMissionsContainer) {
        val pioneerMissions : List<PioneerMission> = playerMissionContainer.findMissions(PioneerMission::class.java)

        val improvementsPlanDestinationsScore = generateImprovementsPlanScore(player, AddImprovementPolicy.Balanced())

        var potentialImprovementsDestinationCount = 0
        for (objectScore in improvementsPlanDestinationsScore) {
            if (objectScore.obj.hasImprovements()) {
                potentialImprovementsDestinationCount++
            }
        }

        // no more new pioneers mission then half of improvements destinations
        val maxPioneersCount: Int = potentialImprovementsDestinationCount / 2
        if (pioneerMissions.size >= maxPioneersCount) {
            // no missions
            return
        }

        val firstDestination : ColonyTilesImprovementPlan? = firstImprovmentColonyDestination(improvementsPlanDestinationsScore, pioneerMissions)
        if (firstDestination != null) {
            val buyPionnierOrder = calculateBuyPionnierOrder(player, pioneerMissions)
            val boughtPioneer : Unit = when (buyPionnierOrder) {
                is BuyPioneerOrder.BuySpecialistOrder -> buyPionnierOrder.buy(player)
                is BuyPioneerOrder.RecruitColonistOrder -> buyPionnierOrder.buy(player, game)
                else -> return
            }
            val pioneerMission = PioneerMission(boughtPioneer, firstDestination.colony)
            playerMissionContainer.addMission(pioneerMission)
        }
    }

    fun findNextColonyToImprove(mission: PioneerMission, playerMissionContainer: PlayerMissionsContainer): ColonyTilesImprovementPlan? {
        val colonyWithMissions = HashSet<String>(playerMissionContainer.player.settlements.size())
        playerMissionContainer.findMissionAndConsume(PioneerMission::class.java, { pioneerMission ->
            colonyWithMissions.add(pioneerMission.colonyId)
        })

        val improvementsPlanDestinationsScore = generateImprovementsPlanScore(playerMissionContainer.player, AddImprovementPolicy.Balanced())
        pathFinder.generateRangeMap(game.map, mission.pioneer, PathFinder.includeUnexploredTiles)

        var improvementDestination: ColonyTilesImprovementPlan? = null
        var destinationScore: Double = 0.0

        for (planScore in improvementsPlanDestinationsScore) {
            if (planScore.score == 0 || colonyWithMissions.contains(planScore.obj.colony.id)) {
                continue
            }
            val dscore: Double = planScore.score.toDouble() / nextColonyDistanceValue(planScore.obj.colony)
            if (improvementDestination == null || dscore > destinationScore) {
                destinationScore = dscore
                improvementDestination = planScore.obj
            }
        }
        return improvementDestination
    }

    private fun nextColonyDistanceValue(colony: Colony): Int {
        var distance = pathFinder.turnsCost(colony.tile)
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
        pioneerMissions : List<PioneerMission>
    ): ColonyTilesImprovementPlan? {
        for (destinationScore in improvementsPlanDestinationsScore) {
            if (destinationScore.obj.hasImprovements() && !hasMissionToColony(pioneerMissions, destinationScore.obj.colony)) {
                return destinationScore.obj
            }
        }
        return null
    }

    fun calculateBuyPionnierOrder(player: Player, pioneerMissions: List<PioneerMission>): BuyPioneerOrder {
        var hasSpecialist = false
        for (pioneerMission in pioneerMissions) {
            if (pioneerMission.isSpecialist()) {
                hasSpecialist = true
                break
            }
        }

        val hardyPioneerUnitType = Specification.instance.unitTypes.getById(UnitType.HARDY_PIONEER)
        val hardyPioneerPrice = player.europe.aiUnitPrice(hardyPioneerUnitType)
        if (player.hasGold(hardyPioneerPrice * 2)) {
            return BuyPioneerOrder.BuySpecialistOrder(hardyPioneerUnitType)
        } else if (!hasSpecialist) {
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

    private fun hasMissionToColony(pioneerMissions: List<PioneerMission>, colony: Colony): Boolean {
        for (pioneerMission in pioneerMissions) {
            if (pioneerMission.isToColony(colony)) {
                return true
            }
        }
        return false
    }
}