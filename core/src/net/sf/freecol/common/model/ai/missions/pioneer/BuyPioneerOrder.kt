package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.ReplaceColonyWorkerMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.player.Player

/**
 * Buy unit in europe so transport is required.
 */
sealed class BuyPioneerOrder {

    class BuySpecialistOrder: BuyPioneerOrder() {

        override fun buyAndPrepareMissions(playerMissionContainer: PlayerMissionsContainer, pioneerDestination: Colony, game: Game) {
            val boughtPioneer = buy(playerMissionContainer.player)
            val pioneerMission = PioneerMission(boughtPioneer, pioneerDestination)
            playerMissionContainer.addMission(pioneerMission)
            val transportUnitRequestMission = TransportUnitRequestMission(game.turn, boughtPioneer, pioneerDestination.tile)
                .withCheckAvailability()
            playerMissionContainer.addMission(pioneerMission, transportUnitRequestMission)
        }

        fun buy(player: Player): Unit {
            val hardyPioneerUnitType = Specification.instance.unitTypes.getById(UnitType.HARDY_PIONEER)
            return player.europe.buyUnitByAI(hardyPioneerUnitType)
        }
    }

    class RecruitColonistOrder: BuyPioneerOrder() {

        override fun buyAndPrepareMissions(playerMissionContainer: PlayerMissionsContainer, pioneerDestination: Colony, game: Game) {
            val boughtPioneer = buy(playerMissionContainer.player, game)
            val pioneerMission = PioneerMission(boughtPioneer, pioneerDestination)
            playerMissionContainer.addMission(pioneerMission)
            val transportUnitRequestMission = TransportUnitRequestMission(game.turn, boughtPioneer, pioneerDestination.tile)
                .withCheckAvailability()
            playerMissionContainer.addMission(pioneerMission, transportUnitRequestMission)
        }

        fun buy(player: Player, game: Game): Unit {
            val freeColonistUnitType = Specification.instance.freeColonistUnitType
            val pioneerRole = Specification.instance.unitRoles.getById(UnitRole.PIONEER)

            val freeColonist = player.europe.buyUnitByAI(freeColonistUnitType)
            player.europe.changeUnitRole(game, freeColonist, pioneerRole)
            return freeColonist
        }
    }

    class RecruitColonistWithToolsAndHardyPionnerLocation(val toolsColonySource: Colony, val colonyHardyPioneer: ColonyHardyPioneer): BuyPioneerOrder() {
        override fun buyAndPrepareMissions(playerMissionContainer: PlayerMissionsContainer, pioneerDestination: Colony, game: Game) {
            val pioneerRole = Specification.instance.unitRoles.getById(UnitRole.PIONEER)

            val boughtPioneer = buy(playerMissionContainer.player)

            val pioneerMission = PioneerMission(boughtPioneer, pioneerDestination)
            val takeRoleMission = TakeRoleEquipmentMission(boughtPioneer, toolsColonySource, pioneerRole)
            takeRoleMission.makeReservation(game, playerMissionContainer.player)

            val replaceColonyWorkerMission = ReplaceColonyWorkerMission(colonyHardyPioneer.colony, colonyHardyPioneer.hardyPioneer, boughtPioneer)
            val transportRequest = TransportUnitRequestMission(game.turn, boughtPioneer, colonyHardyPioneer.colony.tile)

            playerMissionContainer.addMission(pioneerMission)
            playerMissionContainer.addMission(pioneerMission, takeRoleMission)
            playerMissionContainer.addMission(takeRoleMission, replaceColonyWorkerMission)
            playerMissionContainer.addMission(replaceColonyWorkerMission, transportRequest)
        }

        private fun buy(player: Player): Unit {
            val freeColonistUnitType = Specification.instance.freeColonistUnitType
            val colonist = player.europe.buyUnitByAI(freeColonistUnitType)
            return colonist
        }
    }

    class RecruitColonistWithHardyPioneerLocation(val colonyHardyPioneer: ColonyHardyPioneer) : BuyPioneerOrder() {
        override fun buyAndPrepareMissions(playerMissionContainer: PlayerMissionsContainer, pioneerDestination: Colony, game: Game) {
            val boughtPioneer = buy(playerMissionContainer.player, game)

            val pioneerMission = PioneerMission(boughtPioneer, pioneerDestination)
            val replaceColonyWorkerMission = ReplaceColonyWorkerMission(colonyHardyPioneer.colony, colonyHardyPioneer.hardyPioneer, boughtPioneer)
            val transportRequest = TransportUnitRequestMission(game.turn, boughtPioneer, colonyHardyPioneer.colony.tile)

            playerMissionContainer.addMission(pioneerMission)
            playerMissionContainer.addMission(pioneerMission, replaceColonyWorkerMission)
            playerMissionContainer.addMission(replaceColonyWorkerMission, transportRequest)
        }

        private fun buy(player: Player, game: Game): Unit {
            val freeColonistUnitType = Specification.instance.freeColonistUnitType
            val pioneerRole = Specification.instance.unitRoles.getById(UnitRole.PIONEER)

            val freeColonist = player.europe.buyUnitByAI(freeColonistUnitType)
            player.europe.changeUnitRole(game, freeColonist, pioneerRole)
            return freeColonist
        }
    }

    class RecruitColonistWithToolsLocation(val equiptLocation: Colony) : BuyPioneerOrder() {

        override fun buyAndPrepareMissions(playerMissionContainer: PlayerMissionsContainer, pioneerDestination: Colony, game: Game) {
            val boughtPioneer = buy(playerMissionContainer.player)
            val pioneerRole = Specification.instance.unitRoles.getById(UnitRole.PIONEER)

            val pioneerMission = PioneerMission(boughtPioneer, pioneerDestination)
            val takeRoleMission = TakeRoleEquipmentMission(boughtPioneer, equiptLocation, pioneerRole)
            takeRoleMission.makeReservation(game, playerMissionContainer.player)

            val transportRequest = TransportUnitRequestMission(game.turn, boughtPioneer, equiptLocation.tile)

            playerMissionContainer.addMission(pioneerMission)
            playerMissionContainer.addMission(pioneerMission, takeRoleMission)
            playerMissionContainer.addMission(takeRoleMission, transportRequest)
        }

        private fun buy(player: Player): Unit {
            val freeColonistUnitType = Specification.instance.freeColonistUnitType
            val colonist = player.europe.buyUnitByAI(freeColonistUnitType)
            return colonist
        }
    }

    class CanNotAfford: BuyPioneerOrder() {
        override fun buyAndPrepareMissions(playerMissionContainer: PlayerMissionsContainer, pioneerDestination: Colony, game: Game) {
            // can not afford
        }
    }

    abstract fun buyAndPrepareMissions(playerMissionContainer: PlayerMissionsContainer, pioneerDestination: Colony, game: Game)

}
