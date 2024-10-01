package net.sf.freecol.common.model.player

import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Turn
import promitech.map.Byte2dArray

class PlayerExploredTiles(width: Int, height: Int, turn: Turn) {

    private val tiles = Byte2dArray(width, height)
    private var lastRollTurnNumber = (turn.number / TURNS_RANGE) * TURNS_RANGE

    fun exploredTurn(x: Int, y: Int): Byte {
        return tiles.get(x, y)
    }

    fun isTileExplored(coordsIndex: Int): Boolean {
        return tiles.get(coordsIndex) != UNEXPLORED
    }

    fun isTileExplored(x: Int, y: Int): Boolean {
        return tiles.get(x, y) != UNEXPLORED
    }

    fun isTileUnExplored(x: Int, y: Int): Boolean {
        return tiles.get(x, y) == UNEXPLORED
    }

    fun init(tile: Tile, turn: Byte) {
        tiles.set(tile.x, tile.y, turn)
    }

    fun init(x: Int, y: Int, turn: Byte) {
        tiles.set(x, y, turn)
    }

    /**
     * return boolean true when explore tile
     */
    fun setAndReturnDifference(coordsIndex: Int, turn: Turn): Boolean {
        roll(turn.number)
        val c = tiles.get(coordsIndex) == UNEXPLORED
        tiles.set(coordsIndex, ranged(turn.number))
        return c
    }

    fun setTileAsExplored(tile: Tile, turn: Turn) {
        roll(turn.number)
        tiles.set(tile.x, tile.y, ranged(turn.number))
    }

    fun setTileAsExplored(coordsIndex: Int, turn: Turn) {
        roll(turn.number)
        tiles.set(coordsIndex, ranged(turn.number))
    }

    fun setTileAsExplored(x: Int, y: Int, turn: Turn) {
        roll(turn.number)
        tiles.set(x, y, ranged(turn.number))
    }

    fun exploreAllTiles(turn: Turn) {
        roll(turn.number)
        tiles.set(ranged(turn.number))
    }

    private fun ranged(turnNumber: Int): Byte {
        return ((turnNumber % TURNS_RANGE) + TURN_PREFIX).toByte()
    }

    private fun roll(turnNumber: Int) {
        if (lastRollTurnNumber == turnNumber) {
            return
        }
        val rollCount = turnNumber / TURNS_RANGE - lastRollTurnNumber / TURNS_RANGE
        if (rollCount == 0) {
            lastRollTurnNumber = turnNumber
            return
        }
        lastRollTurnNumber = turnNumber
        for (i in 0 until tiles.cellLength()) {
            val v = tiles.get(i)
            if (v != UNEXPLORED) {
                if (v > TURNS_RANGE) {
                    tiles.set(i, (v - TURNS_RANGE * rollCount).toByte())
                } else {
                    tiles.set(i, FIRST)
                }
            }
        }
    }

    companion object {
        @JvmField
        val UNEXPLORED: Byte = 0
        @JvmField
        val FIRST: Byte = 1
        val TURNS_RANGE: Byte = 30
        val TURN_PREFIX: Byte = 90
    }

}