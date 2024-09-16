package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.AbstractMission
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.ReplaceColonyWorkerMission
import net.sf.freecol.common.model.ai.missions.ReplaceUnitInMissionHandler
import net.sf.freecol.common.model.ai.missions.hasMission
import net.sf.freecol.common.model.ai.missions.transportunit.CheckAvailabilityMissionHandler
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.util.whenNotNull
import promitech.colonization.ai.CommonMissionHandler
import promitech.colonization.ai.CommonMissionHandler.isColonyOwner
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
        val pioneer: Unit? = player.units.getByIdOrNull(mission.pioneerId)

        if (pioneer == null || !CommonMissionHandler.isUnitExists(player, pioneer)) {
            MissionHandlerLogger.logger.debug("player[%s].PioneerMissionHandler pioneer does not exists", player.getId())
            mission.setDone()
            return
        }
        if (!isColonyOwner(pioneer.owner, mission.colonyId)) {
            MissionHandlerLogger.logger.debug("player[%s].PioneerMissionHandler other colony owner", player.getId())
            mission.setDone()
            return
        }

        if (pioneer.isAtTileLocation) {
            improveTitles(playerMissionsContainer, mission, pioneer)
        } else if (pioneer.isAtUnitLocation) {
            // do nothing, wait for transport
            return
        } else if (pioneer.isAtEuropeLocation) {
            createTransportRequest(game, playerMissionsContainer, mission, pioneer, mission.colonyTile(player))
        }
    }

    private fun improveTitles(playerMissionsContainer: PlayerMissionsContainer, mission: PioneerMission, pioneer: Unit) {
        if (pioneer.isWorkingOnImprovement) {
            return
        }

        val improvementDestination = pioneerMissionPlaner.findImprovementDestination(mission, playerMissionsContainer, pioneer)
        processImprovementDestination(playerMissionsContainer, mission, pioneer, improvementDestination)
    }

    private fun processImprovementDestination(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: PioneerMission,
        pioneer: Unit,
        improvementDestination: PioneerDestination
    ) {
        when (improvementDestination) {
            is PioneerDestination.TheSameIsland -> {
                mission.changeColony(improvementDestination.plan.colony)

                if (!isSpecialist(pioneer) && tryCreateHardyPioneerToReplaceMission(playerMissionsContainer, mission, pioneer, improvementDestination.plan.colony.tile)) {
                    return
                }

                if (isPioneerWithoutTools(pioneer)) {
                    if (tryCreateTakeRoleEquipmentMission(playerMissionsContainer, mission, pioneer, improvementDestination.plan.colony.tile)) {
                        return
                    }
                    gotoColonyAndWaitForTools(playerMissionsContainer, mission, pioneer)
                    return
                }

                val firstImprovement = improvementDestination.plan.firstImprovement()
                moveToDestination(game, moveService, pathFinder, pioneer, firstImprovement.tile) {
                    startImprove(playerMissionsContainer, pioneer, firstImprovement)
                }
            }

            is PioneerDestination.OtherIsland -> {
                mission.changeColony(improvementDestination.plan.colony)
                createTransportRequest(game, playerMissionsContainer, mission, pioneer, mission.colonyTile(pioneer.owner))
            }
            is PioneerDestination.Lack -> {
                val colony = mission.colony(pioneer.owner)
                moveToDestination(game, moveService, pathFinder, pioneer, colony.tile) {
                    mission.waitOrResolveFreeColonistPioneer(colony, pioneer)
                }
            }
        }
    }

    private fun tryCreateTakeRoleEquipmentMission(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: PioneerMission,
        pioneer: Unit,
        improveDestTile: Tile
    ): Boolean {
        val colonyToEquiptPioneerInRange = pioneerMissionPlaner.findColonyToEquiptPioneerInRange(
            playerMissionsContainer.player,
            improveDestTile,
            mission.id
        )
        if (colonyToEquiptPioneerInRange != null) {
            val pioneerRole = Specification.instance.unitRoles.getById(UnitRole.PIONEER)
            val takeRoleEquipmentMission = TakeRoleEquipmentMission(pioneer, colonyToEquiptPioneerInRange, pioneerRole)
            playerMissionsContainer.addMission(mission, takeRoleEquipmentMission)
            return true
        }
        return false
    }

    private fun tryCreateHardyPioneerToReplaceMission(
        playerMissionsContainer: PlayerMissionsContainer,
        mission: PioneerMission,
        pioneer: Unit,
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
                pioneer
            )
            playerMissionsContainer.addMission(mission, replaceColonyWorkerMission)
            return true
        }
        return false
    }

    private fun startImprove(playerMissionsContainer: PlayerMissionsContainer, pioneer: Unit, improvementDest: TileImprovementPlan) {
        if (!pioneer.hasMovesPoints()) {
            return
        }
        pioneer.startImprovement(improvementDest.tile, improvementDest.improvementType)

        if (MissionHandlerLogger.logger.isDebug) {
            MissionHandlerLogger.logger.debug("player[%s].PioneerMissionHandler start improvement %s on tile [%s], turns to complete %s",
                playerMissionsContainer.player.id,
                improvementDest.improvementType.toSmallIdStr(),
                improvementDest.tile.toStringCords(),
                pioneer.workTurnsToComplete()
            )
        }
    }

    private fun gotoColonyAndWaitForTools(playerMissionsContainer: PlayerMissionsContainer, mission: PioneerMission, pioneer: Unit) {
        val colony = mission.colony(pioneer.owner)
        moveToDestination(game, moveService, pathFinder, pioneer, colony.tile) {
            equipTools(playerMissionsContainer, mission, pioneer, colony)
        }
    }

    private fun equipTools(playerMissionsContainer: PlayerMissionsContainer, mission: PioneerMission, pioneer: Unit, colony: Colony) {
        val playerAiContainer = game.aiContainer.playerAiContainer(playerMissionsContainer.player)
        val pioneerRole = Specification.instance.unitRoles.getById(UnitRole.PIONEER)

        val colonySupplyGoods = playerAiContainer.findColonySupplyGoods(colony)
        if (pioneerMissionPlaner.hasColonyRequiredGoods(colony, pioneerRole.sumOfRequiredGoods(), colonySupplyGoods, mission.id)) {
            colony.changeUnitRole(pioneer, pioneerRole)
            if (colonySupplyGoods != null) {
                colonySupplyGoods.removeSupplyReservation(mission.id)
            }
        } else {
            val requestGoodsMissionExists = playerMissionsContainer.hasMission(RequestGoodsMission::class.java) { requestGoodsMission ->
                requestGoodsMission.purpose.equals(mission.id)
            }
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
            playerMissionsContainer.player.units.getByIdOrNull(parentMission.pioneerId).whenNotNull { pioneer ->
                val improvementDestination = pioneerMissionPlaner.findNextColonyToImprove(
                    pioneer,
                    parentMission.colonyId,
                    playerMissionsContainer
                )
                val nextColonyDestination: Colony? = improvementDestination.extractColonyDestination()
                if (nextColonyDestination != null && !parentMission.isToColony(nextColonyDestination)) {
                    transportRequestMission.setDone()
                    processImprovementDestination(playerMissionsContainer, parentMission, pioneer, improvementDestination)
                }
            }
        }
    }

    private fun isPioneerWithoutTools(pioneer: Unit): Boolean {
        return pioneer.unitRole.equalsId(UnitRole.DEFAULT_ROLE_ID)
    }


    companion object {
        internal fun isSpecialist(pioneer: Unit): Boolean {
            return pioneer.unitType.isType(UnitType.HARDY_PIONEER)
        }
    }

}