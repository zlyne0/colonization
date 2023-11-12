package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.hasMissionKt
import net.sf.freecol.common.model.ai.missions.transportunit.CheckAvailabilityMissionHandler
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.map.path.PathFinder
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger
import promitech.colonization.ai.createTransportRequest
import promitech.colonization.ai.moveToDestination
import promitech.colonization.orders.move.MoveService

class PioneerMissionHandler(
    private val game: Game,
    private val pioneerMissionPlaner: PioneerMissionPlaner,
    private val moveService: MoveService,
    private val pathFinder: PathFinder
): MissionHandler<PioneerMission>, ReplaceUnitInMissionHandler, CheckAvailabilityMissionHandler {

    override fun handle(playerMissionsContainer: PlayerMissionsContainer, mission: PioneerMission) {
        val player = playerMissionsContainer.player

        if (!mission.isPioneerExists()) {
            MissionHandlerLogger.logger.debug("player[%s].PioneerMissionHandler pioneer does not exists", player.getId())
            mission.setDone()
            return
        }
        if (!mission.isColonyOwner()) {
            MissionHandlerLogger.logger.debug("player[%s].PioneerMissionHandler other colony owner", player.getId())
            mission.setDone()
            return
        }

        if (mission.pioneer.isAtTileLocation) {
            improveTitles(playerMissionsContainer, mission)
        } else if (mission.pioneer.isAtUnitLocation) {
            // do nothing, wait for transport
            return
        } else if (mission.pioneer.isAtEuropeLocation) {
            createTransportRequest(game, playerMissionsContainer, mission, mission.pioneer, mission.colony().tile)
        }
    }

    private fun improveTitles(playerMissionsContainer: PlayerMissionsContainer, mission: PioneerMission) {
        if (mission.pioneer.isWorkingOnImprovement) {
            return
        }

        val improvementDestination = pioneerMissionPlaner.findImprovementDestination(mission, playerMissionsContainer)
        processImprovementDestination(playerMissionsContainer, mission, improvementDestination)
    }

    private fun processImprovementDestination(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: PioneerMission,
        improvementDestination: PioneerDestination
    ) {
        when (improvementDestination) {
            is PioneerDestination.TheSameIsland -> {
                mission.changeColony(improvementDestination.plan.colony)

                if (!mission.isSpecialist() && tryCreateHardyPioneerToReplaceMission(playerMissionsContainer, mission, improvementDestination.plan.colony.tile)) {
                    return
                }

                if (mission.isPioneerWithoutTools()) {
                    if (tryCreateTakeRoleEquipmentMission(playerMissionsContainer, mission, improvementDestination.plan.colony.tile)) {
                        return
                    }
                    gotoColonyAndWaitForTools(playerMissionsContainer, mission)
                    return
                }

                val firstImprovement = improvementDestination.plan.firstImprovement()
                moveToDestination(game, moveService, pathFinder, mission.pioneer, firstImprovement.tile) {
                    startImprove(playerMissionsContainer, mission, firstImprovement)
                }
            }

            is PioneerDestination.OtherIsland -> {
                mission.changeColony(improvementDestination.plan.colony)
                createTransportRequest(game, playerMissionsContainer, mission, mission.pioneer, mission.colony().tile)
            }
            is PioneerDestination.Lack -> {
                val colony = mission.colony()
                moveToDestination(game, moveService, pathFinder, mission.pioneer, colony.tile) {
                    mission.waitOrResolveFreeColonistPionner(colony)
                }
            }
        }
    }

    private fun tryCreateTakeRoleEquipmentMission(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: PioneerMission,
        improveDestTile: Tile
    ): Boolean {
        val colonyToEquiptPioneerInRange = pioneerMissionPlaner.findColonyToEquiptPioneerInRange(
            playerMissionsContainer.player,
            improveDestTile,
            mission.id
        )
        if (colonyToEquiptPioneerInRange != null) {
            val pioneerRole = Specification.instance.unitRoles.getById(UnitRole.PIONEER)
            val takeRoleEquipmentMission = TakeRoleEquipmentMission(mission.pioneer, colonyToEquiptPioneerInRange, pioneerRole)
            playerMissionsContainer.addMission(mission, takeRoleEquipmentMission)
            return true
        }
        return false
    }

    private fun tryCreateHardyPioneerToReplaceMission(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: PioneerMission,
        improveDestTile: Tile
    ): Boolean {
        val colonyHardyPioneerInRange = pioneerMissionPlaner.findColonyHardyPioneerInRange(
            playerMissionsContainer.player,
            improveDestTile
        )
        if (colonyHardyPioneerInRange != null) {
            val replaceColonyWorkerMission = ReplaceColonyWorkerMission(
                colonyHardyPioneerInRange.colony,
                colonyHardyPioneerInRange.hardyPioneer,
                mission.pioneer
            )
            playerMissionsContainer.addMission(mission, replaceColonyWorkerMission)
            return true
        }
        return false
    }

    private fun startImprove(playerMissionsContainer: PlayerMissionsContainer, mission: PioneerMission, improvementDest: TileImprovementPlan) {
        if (!mission.pioneer.hasMovesPoints()) {
            return
        }
        mission.pioneer.startImprovement(improvementDest.tile, improvementDest.improvementType)

        if (MissionHandlerLogger.logger.isDebug) {
            MissionHandlerLogger.logger.debug("player[%s].PioneerMissionHandler start improvement %s on tile [%s], turns to complete %s",
                playerMissionsContainer.player.id,
                improvementDest.improvementType.toSmallIdStr(),
                improvementDest.tile.toStringCords(),
                mission.pioneer.workTurnsToComplete()
            )
        }
    }

    private fun gotoColonyAndWaitForTools(playerMissionsContainer: PlayerMissionsContainer, mission: PioneerMission) {
        val colony = mission.colony()
        moveToDestination(game, moveService, pathFinder, mission.pioneer, colony.tile) {
            equipTools(playerMissionsContainer, mission, colony)
        }
    }

    private fun equipTools(playerMissionsContainer: PlayerMissionsContainer, mission: PioneerMission, colony: Colony) {
        val playerAiContainer = game.aiContainer.playerAiContainer(playerMissionsContainer.player)
        val pioneerRole = Specification.instance.unitRoles.getById(UnitRole.PIONEER)

        val colonySupplyGoods = playerAiContainer.findColonySupplyGoods(colony)
        if (pioneerMissionPlaner.hasColonyRequiredGoods(colony, pioneerRole.sumOfRequiredGoods(), colonySupplyGoods, mission.id)) {
            colony.changeUnitRole(mission.pioneer, pioneerRole)
            if (colonySupplyGoods != null) {
                colonySupplyGoods.removeSupplyReservation(mission.id)
            }
        } else {
            val requestGoodsMissionExists = playerMissionsContainer.hasMissionKt(RequestGoodsMission::class.java, { requestGoodsMission ->
                requestGoodsMission.purpose.equals(mission.id)
            })
            if (!requestGoodsMissionExists) {
                val requiredGoods = pioneerRole.sumOfRequiredGoods()
                playerMissionsContainer.addMission(mission, RequestGoodsMission(colony, requiredGoods, mission.id))
            }
        }
    }

    override fun replaceUnitInMission(mission: AbstractMission, unitToReplace: Unit, replaceBy: Unit) {
        if (mission is PioneerMission) {
            val playerMissionsContainer = game.aiContainer.missionContainer(replaceBy.owner)
            mission.changeUnit(unitToReplace, replaceBy, playerMissionsContainer)
        }
    }

    override fun checkAvailability(
        playerMissionsContainer: PlayerMissionsContainer,
        parentMission: AbstractMission,
        transportRequestMission: TransportUnitRequestMission
    ) {
        if (parentMission is PioneerMission) {
            val improvementDestination = pioneerMissionPlaner.findNextColonyToImprove(
                parentMission.pioneer,
                parentMission.colonyId,
                playerMissionsContainer
            )
            val nextColonyDestination: Colony? = improvementDestination.extractColonyDestination()
            if (nextColonyDestination != null && !parentMission.isToColony(nextColonyDestination)) {
                transportRequestMission.setDone()
                processImprovementDestination(playerMissionsContainer, parentMission, improvementDestination)
            }
        }
    }
}