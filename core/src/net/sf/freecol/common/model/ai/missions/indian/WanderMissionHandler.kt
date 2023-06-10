package net.sf.freecol.common.model.ai.missions.indian

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.MoveType
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.map.path.Path
import net.sf.freecol.common.model.map.path.PathFinder
import promitech.colonization.Direction
import promitech.colonization.Randomizer
import promitech.colonization.ai.MissionHandler
import promitech.colonization.ai.MissionHandlerLogger.logger
import promitech.colonization.orders.combat.CombatService
import promitech.colonization.orders.move.MoveContext
import promitech.colonization.orders.move.MoveService

class WanderMissionHandler(
    val game: Game,
    val moveService: MoveService,
    val pathFinder: PathFinder,
    val combatService: CombatService
) : MissionHandler<WanderMission> {

    private val settlementWanderRange = SettlementWanderRange(game.map)

    private val allowedDirections: MutableList<Direction> = ArrayList(Direction.allDirections)
    private val moveContext = MoveContext()

    override fun handle(playerMissionsContainer: PlayerMissionsContainer, mission: WanderMission) {
        if (mission.unit.isDisposed) {
            mission.setDone()
            return
        }
        settlementWanderRange.prepare(mission.unit.owner)

        while (true) {
            val aggressionTile = AttackDecision.determineTileToAggression(game.map, mission.unit)
            if (aggressionTile != null) {
                moveToAggressionTile(mission, aggressionTile)
                break
            }
            if (!moveToRandomDirection(mission)) {
                break
            }
        }
    }

    private fun moveToAggressionTile(mission: WanderMission, aggressionTile: Tile) {
        if (logger.isDebug) {
            logger.debug("player[%s].wanderMissionHandler[%s].tileToAggression[%s]",
                mission.unit.owner.id,
                mission.id,
                aggressionTile.toStringCords()
            )
        }
        val pathToEnemy: Path = pathFinder.findToTile(game.map, mission.unit.tile, aggressionTile, mission.unit, PathFinder.includeUnexploredTiles)
        if (pathToEnemy.isReachedDestination) {
            val moveContext = MoveContext(mission.unit, pathToEnemy)
            moveContext.initNextPathStep()
            while (moveContext.canAiHandleMove()) {
                if (moveContext.isMoveType(MoveType.ATTACK_UNIT) || moveContext.isMoveType(MoveType.ATTACK_SETTLEMENT)) {
                    combatService.doCombat(moveContext)
                    if (mission.unit.isDisposed) {
                        mission.setDone()
                    }
                    break;
                }
                moveService.aiConfirmedMoveProcessor(moveContext)
                moveContext.initNextPathStep()
            }
        }
    }

    private fun moveToRandomDirection(mission: WanderMission): Boolean {
        val sourceTile = mission.unit.tile
        val moveDirection = generateRandomDirection(mission, sourceTile)
        if (moveDirection == null) {
            mission.previewDirection = null
            return false
        }
        val destTile = game.map.getTile(sourceTile, moveDirection)
        moveContext.init(sourceTile, destTile, mission.unit, moveDirection)
        if (moveContext.canAiHandleMove()) {
            moveService.aiConfirmedMoveProcessor(moveContext)
            mission.previewDirection = moveDirection
            return mission.unit.hasMovesPoints()
        }
        return false
    }

    private fun generateRandomDirection(mission: WanderMission, sourceTile: Tile): Direction? {
        val comeDirection: Direction? = mission.previewReverseDirection()

        allowedDirections.clear()

        for (direction in Direction.allDirections) {
            val neighbourTile = game.map.getTile(sourceTile, direction) ?: continue
            if (comeDirection != null && comeDirection == direction) {
                continue
            }
            if (!settlementWanderRange.inRange(neighbourTile)) {
                continue
            }

            moveContext.init(sourceTile, neighbourTile, mission.unit, direction)
            if (moveContext.canAiHandleMove() && moveContext.isMoveType) {
                allowedDirections.add(direction)
            }
        }
        return if (allowedDirections.isEmpty()) {
            null
        } else Randomizer.instance().randomMember(allowedDirections)
    }

}