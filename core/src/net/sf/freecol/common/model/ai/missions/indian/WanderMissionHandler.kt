package net.sf.freecol.common.model.ai.missions.indian

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.MoveType
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer
import net.sf.freecol.common.model.map.path.Path
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import promitech.colonization.Direction
import promitech.colonization.Randomizer
import promitech.colonization.ai.CommonMissionHandler
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
        val player = playerMissionsContainer.player
        val unit: Unit? = player.units.getByIdOrNull(mission.unitId)

        if (unit == null || !CommonMissionHandler.isUnitExists(player, unit)) {
            mission.setDone()
            return
        }
        settlementWanderRange.prepare(unit.owner)

        while (true) {
            val aggressionTile = AttackDecision.determineTileToAggression(game.map, unit)
            if (aggressionTile != null) {
                moveToAggressionTile(mission, aggressionTile, player, unit)
                break
            }
            if (!moveToRandomDirection(mission, unit)) {
                break
            }
        }
    }

    private fun moveToAggressionTile(mission: WanderMission, aggressionTile: Tile, player: Player, unit: Unit) {
        if (logger.isDebug) {
            logger.debug("player[%s].wanderMissionHandler[%s].tileToAggression[%s]",
                player.id,
                mission.id,
                aggressionTile.toStringCords()
            )
        }
        val pathToEnemy: Path = pathFinder.findToTile(game.map, unit.tile, aggressionTile, unit, PathFinder.includeUnexploredTiles)
        if (pathToEnemy.isReachedDestination) {
            val moveContext = MoveContext(unit, pathToEnemy)
            moveContext.initNextPathStep()
            while (moveContext.canAiHandleMove()) {
                if (moveContext.isMoveType(MoveType.ATTACK_UNIT) || moveContext.isMoveType(MoveType.ATTACK_SETTLEMENT)) {
                    combatService.doCombat(moveContext)
                    if (unit.isDisposed) {
                        mission.setDone()
                    }
                    break;
                }
                moveService.aiConfirmedMoveProcessor(moveContext)
                moveContext.initNextPathStep()
            }
        }
    }

    private fun moveToRandomDirection(mission: WanderMission, unit: Unit): Boolean {
        val sourceTile = unit.tile
        val moveDirection = generateRandomDirection(mission, sourceTile, unit)
        if (moveDirection == null) {
            mission.previewDirection = null
            return false
        }
        val destTile = game.map.getTile(sourceTile, moveDirection)
        moveContext.init(sourceTile, destTile, unit, moveDirection)
        if (moveContext.canAiHandleMove()) {
            moveService.aiConfirmedMoveProcessor(moveContext)
            mission.previewDirection = moveDirection
            return unit.hasMovesPoints()
        }
        return false
    }

    private fun generateRandomDirection(mission: WanderMission, sourceTile: Tile, unit: Unit): Direction? {
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

            moveContext.init(sourceTile, neighbourTile, unit, direction)
            if (moveContext.canAiHandleMove() && moveContext.isMoveType) {
                allowedDirections.add(direction)
            }
        }
        return if (allowedDirections.isEmpty()) {
            null
        } else Randomizer.instance().randomMember(allowedDirections)
    }

}