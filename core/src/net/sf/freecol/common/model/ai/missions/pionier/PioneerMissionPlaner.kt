package net.sf.freecol.common.model.ai.missions.pionier

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.ProductionSummary
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import promitech.colonization.ai.score.ObjectScoreList

class PioneerMissionPlaner(val game: Game, val pathFinder: PathFinder) {

    fun plan(player: Player, playerMissionContainer: PlayerMissionsContainer) {
        val pionierMissions : List<PionierMission> = playerMissionContainer.findMissions(PionierMission::class.java)

        val improvementsPlanDestinationsScore = generateImprovementsPlanScore(player, AddImprovementPolicy.Balanced())

        var potentialImprovementsDestinationCount = 0
        for (objectScore in improvementsPlanDestinationsScore) {
            if (objectScore.obj.hasImprovements()) {
                potentialImprovementsDestinationCount++
            }
        }

        // no more new pioniers mission then half of improvements destinations
        val maxPioniersCount: Int = potentialImprovementsDestinationCount / 2
        if (pionierMissions.size >= maxPioniersCount) {
            // no missions
            return
        }

        val firstDestination : ColonyTilesImprovementPlan? = firstImprovmentColonyDestination(improvementsPlanDestinationsScore, pionierMissions)
        if (firstDestination != null) {
            val buyPionnierOrder = calculateBuyPionnierOrder(player, pionierMissions)
            val boughtPioneer : Unit = when (buyPionnierOrder) {
                is BuyPioneerOrder.BuySpecialistOrder -> buyPionnierOrder.buy(player)
                is BuyPioneerOrder.RecruitColonistOrder -> buyPionnierOrder.buy(player, game)
                else -> return
            }
            val pionierMission = PionierMission(boughtPioneer, firstDestination.colony)
            playerMissionContainer.addMission(pionierMission)
        }
    }

    private fun firstImprovmentColonyDestination(
        improvementsPlanDestinationsScore: ObjectScoreList<ColonyTilesImprovementPlan>,
        pionierMissions : List<PionierMission>
    ): ColonyTilesImprovementPlan? {
        for (destinationScore in improvementsPlanDestinationsScore) {
            if (destinationScore.obj.hasImprovements() && !hasMissionToColony(pionierMissions, destinationScore.obj.colony)) {
                return destinationScore.obj
            }
        }
        return null
    }

    fun calculateBuyPionnierOrder(player: Player, pionierMissions: List<PionierMission>): BuyPioneerOrder {
        var hasSpecialist = false
        for (pionierMission in pionierMissions) {
            if (pionierMission.isSpecialist()) {
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
            val pioneerSumOfRequiredGoods : ProductionSummary = pioneerRole.sumOfRequiredGoods()
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

    private fun hasMissionToColony(pionierMissions: List<PionierMission>, colony: Colony): Boolean {
        for (pionierMission in pionierMissions) {
            if (pionierMission.isToColony(colony)) {
                return true
            }
        }
        return false
    }
}