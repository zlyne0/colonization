package net.sf.freecol.common.model.player

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Map
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Turn
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.util.Predicate
import net.sf.freecol.common.util.whenNotNull
import promitech.colonization.SpiralIterator
import promitech.map.Byte2dArray

class PlayerExploredTiles(width: Int, height: Int, turn: Turn) {

    private val tiles = Byte2dArray(width, height)
    private val spiralIterator = SpiralIterator(width, height)
    private var lastRollTurnNumber = (turn.number / TURNS_RANGE) * TURNS_RANGE

    fun resetFogOfWar(game: Game, player: Player, turn: Turn) {
        roll(turn.number)
        val rangedTurnValue = ranged(turn.number)

        resetFogOfWarForUnits(player, rangedTurnValue)
        resetFogOfWarForSettlements(player, rangedTurnValue)
        resetFogOfWarForMissionary(player, game, rangedTurnValue)
    }

    private fun resetFogOfWarForUnits(player: Player, rangedTurnValue: Byte) {
        for (unit in player.units) {
            unit.tileLocationOrNull.whenNotNull { unitTile ->
                val radius = unit.lineOfSight()
                resetFogOfWarForNeighboursTiles(unitTile, radius, rangedTurnValue)
            }
        }
    }

    private fun resetFogOfWarForSettlements(player: Player, rangedTurnValue: Byte) {
        for (settlement in player.settlements) {
            val visibleRadius = settlement.settlementType.visibleRadius
            resetFogOfWarForNeighboursTiles(settlement.tile, visibleRadius, rangedTurnValue)
        }
    }

    private fun resetFogOfWarForMissionary(player: Player, game: Game, rangedTurnValue: Byte) {
        if (player.isEuropean) {
            for (indianPlayer in game.players) {
                if (indianPlayer.isIndian) {
                    for (settlement in indianPlayer.settlements.entities()) {
                        val indianSettlement = settlement.asIndianSettlement()
                        if (indianSettlement.hasMissionary(player)) {
                            val radius = indianSettlement.missionary.lineOfSight(indianSettlement.settlementType)
                            resetFogOfWarForNeighboursTiles(indianSettlement.tile, radius, rangedTurnValue)
                        }
                    }
                }
            }
        }
    }

    fun resetForOfWar(tile: Tile, radius: Int, turn: Turn) {
        roll(turn.number)
        val rangedTurnValue = ranged(turn.number)
        resetFogOfWarForNeighboursTiles(tile, radius, rangedTurnValue)
    }

    private fun resetFogOfWarForNeighboursTiles(tile: Tile, radius: Int, rangedTurnValue: Byte) {
        tiles.set(tile.x, tile.y, rangedTurnValue)
        spiralIterator.reset(tile.x, tile.y, true, radius)
        var coordsIndex: Int
        while (spiralIterator.hasNext()) {
            coordsIndex = spiralIterator.coordsIndex
            if (isTileExplored(coordsIndex)) {
                tiles.set(coordsIndex, rangedTurnValue)
            }
            spiralIterator.next()
        }
    }

    fun revealMap(map: Map, source: Tile, radius: Int, turn: Turn, tileFilter: Predicate<Tile>) {
        roll(turn.number)
        val rangedTurnValue = ranged(turn.number)

        spiralIterator.reset(source.x, source.y, true, radius)
        while (spiralIterator.hasNext()) {
            val tile = map.getSafeTile(spiralIterator.x, spiralIterator.y)
            if (tileFilter.test(tile)) {
                tiles.set(spiralIterator.coordsIndex, rangedTurnValue)
            }
            spiralIterator.next()
        }
    }

    fun revealMap(source: Tile, radius: Int, turn: Turn) {
        roll(turn.number)
        val rangedTurnValue = ranged(turn.number)

        spiralIterator.reset(source.x, source.y, true, radius)
        while (spiralIterator.hasNext()) {
            tiles.set(spiralIterator.coordsIndex, rangedTurnValue)
            spiralIterator.next()
        }
    }

    /**
     * Method populate [MoveExploredTiles] `exploredTiles` with explored tiles
     */
    fun revealMap(unit: Unit, exploredTiles: MoveExploredTiles, turn: Turn) {
        val unitTileLocation = unit.tile

        roll(turn.number)
        val rangedTurnValue = ranged(turn.number)
        tiles.set(unitTileLocation.x, unitTileLocation.y, rangedTurnValue)

        val radius = unit.lineOfSight()
        spiralIterator.reset(unitTileLocation.x, unitTileLocation.y, true, radius)
        while (spiralIterator.hasNext()) {
            val coordsIndex = spiralIterator.coordsIndex

            val currentVal = tiles.get(coordsIndex)
            if (currentVal == UNEXPLORED) {
                exploredTiles.addExploredTile(spiralIterator.x, spiralIterator.y)
                exploredTiles.addRemoveFogOfWar(spiralIterator.x, spiralIterator.y)
            } else {
                exploredTiles.addRemoveFogOfWar(spiralIterator.x, spiralIterator.y)
            }
            tiles.set(coordsIndex, rangedTurnValue)
            spiralIterator.next()
        }
    }

    fun hasFogOfWar(tile: Tile, actualTurn: Turn): Boolean {
        return tiles.get(tile.x, tile.y) < ranged(actualTurn.number)
    }

    fun hasFogOfWar(x: Int, y: Int, actualTurn: Turn): Boolean {
        return tiles.get(x, y) < ranged(actualTurn.number)
    }

    fun exploredTurn(x: Int, y: Int): Byte {
        return tiles.get(x, y)
    }

    private fun isTileExplored(coordsIndex: Int): Boolean {
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

    fun setTileAsExplored(x: Int, y: Int, turn: Turn) {
        roll(turn.number)
        tiles.set(x, y, ranged(turn.number))
    }

    fun exploreAllTiles(turn: Turn) {
        roll(turn.number)
        tiles.set(ranged(turn.number))
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
        val FIRST: Byte = 1
        val TURNS_RANGE: Byte = 30
        val TURN_PREFIX: Byte = 90
        val RECENTLY_EXPLORED: Byte = Byte.MAX_VALUE

        @JvmStatic
        fun ranged(turnNumber: Int): Byte {
            return ((turnNumber % TURNS_RANGE) + TURN_PREFIX).toByte()
        }

        @JvmStatic
        fun isExplored(b: Byte): Boolean = b != UNEXPLORED
    }

}