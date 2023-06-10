package net.sf.freecol.common.model.ai.missions.indian

import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.forEachTileCoordinatesInRange
import net.sf.freecol.common.model.player.Player
import promitech.map.Boolean2dArray

internal class SettlementWanderRange(
    private val map: net.sf.freecol.common.model.Map,
) {
    private val settlementWanderRange: Boolean2dArray = Boolean2dArray(map.width, map.height)
    private var wanderRangeForPlayer: Player? = null

    fun prepare(player: Player) {
        if (wanderRangeForPlayer != null && wanderRangeForPlayer!!.equalsId(player)) {
            return
        }

        wanderRangeForPlayer = player
        settlementWanderRange.set(false)

        for (settlement in player.settlements) {
            map.forEachTileCoordinatesInRange(settlement.tile, settlement.settlementType.wanderingRadius) { x, y ->
                settlementWanderRange[x, y] = true
            }
        }
    }

    fun inRange(tile: Tile): Boolean {
        return settlementWanderRange.get(tile.x, tile.y)
    }
}