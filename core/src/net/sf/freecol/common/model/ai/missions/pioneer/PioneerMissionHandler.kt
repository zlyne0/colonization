package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.ai.missions.hasMissionKt
import net.sf.freecol.common.model.map.path.Path
import net.sf.freecol.common.model.map.path.PathFinder
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger
import promitech.colonization.orders.move.MoveContext
import promitech.colonization.orders.move.MoveService

class PioneerMissionHandler(
    private val game: Game,
    private val pioneerMissionPlaner: PioneerMissionPlaner,
    private val moveService: MoveService,
    private val pathFinder: PathFinder
): MissionHandler<PioneerMission> {

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
        }
    }

    private fun improveTitles(playerMissionsContainer: PlayerMissionsContainer, mission: PioneerMission) {
        if (mission.pioneer.isWorkingOnImprovement) {
            return
        }
        val improvementsPlan = pioneerMissionPlaner.generateImprovementsPlanForColony(mission.pioneer.owner, mission.colonyId)
        if (improvementsPlan.hasImprovements()) {
            if (mission.pioneer.unitRole.equalsId(UnitRole.DEFAULT_ROLE_ID)) {
                goToColonyAndWaitForTools(playerMissionsContainer, mission)
                return
            }
            val firstImprovement = improvementsPlan.firstImprovement()
            moveToDestination(mission, firstImprovement.tile) {
                startImprove(playerMissionsContainer, mission, firstImprovement)
            }
        } else {
            findNextColonyDestination(playerMissionsContainer, mission)
        }
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

    private fun goToColonyAndWaitForTools(playerMissionsContainer: PlayerMissionsContainer, mission: PioneerMission) {
        val colony = mission.colony()
        moveToDestination(mission, colony.tile) {
            equipTools(playerMissionsContainer, mission, colony)
        }
    }

    private fun equipTools(playerMissionsContainer: PlayerMissionsContainer, mission: PioneerMission, colony: Colony) {
        val pioneerRole = Specification.instance.unitRoles.getById(UnitRole.PIONEER)
        if (colony.hasGoodsToEquipRole(pioneerRole)) {
            colony.changeUnitRole(mission.pioneer, pioneerRole)
        } else {
            val requestGoodsMissionExists = playerMissionsContainer.hasMissionKt(RequestGoodsMission::class.java, { playerMission ->
                playerMission.purpose.equals(mission.id)
            })
            if (!requestGoodsMissionExists) {
                val requiredGoods = pioneerRole.sumOfRequiredGoods()
                mission.addDependMission(RequestGoodsMission(colony, requiredGoods, mission.id))
            }
        }
    }

    private fun findNextColonyDestination(playerMissionsContainer: PlayerMissionsContainer, mission: PioneerMission) {
        val nextPlan: ColonyTilesImprovementPlan? = pioneerMissionPlaner.findNextColonyToImprove(mission, playerMissionsContainer)
        if (nextPlan == null) {
            val colony = mission.colony()
            moveToDestination(mission, colony.tile) {
                mission.waitOrResolveFreeColonistPionner(colony)
            }
        } else {
            mission.changeColony(nextPlan.colony)
            moveToDestination(mission, nextPlan.colony.tile) {}
        }
    }

    private inline fun moveToDestination(mission: PioneerMission, destTile: Tile, action: () -> kotlin.Unit) {
        if (mission.pioneer.isAtLocation(destTile)) {
            action()
        } else {
            val path: Path = pathFinder.findToTile(
                game.map,
                mission.pioneer,
                destTile,
                PathFinder.includeUnexploredTiles
            )
            if (path.reachTile(destTile)) {
                val moveContext = MoveContext(mission.pioneer, path)
                moveService.aiConfirmedMovePath(moveContext)
            }
            if (mission.pioneer.isAtLocation(destTile)) {
                action()
            }
        }
    }

}