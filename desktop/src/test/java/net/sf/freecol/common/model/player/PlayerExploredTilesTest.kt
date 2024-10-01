package net.sf.freecol.common.model.player

import net.sf.freecol.common.model.Turn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlayerExploredTilesTest {

    @Test
    fun can_explore_tiles() {
        // given
        val tiles = PlayerExploredTiles(4, 4, Turn(0))

        // when
        tiles.setTileAsExplored(1, 1, Turn(25))
        tiles.setTileAsExplored(2, 2, Turn(50))
        tiles.setTileAsExplored(3, 3, Turn(100))

        // then
        val exploredTurn1 = tiles.exploredTurn(1, 1)
        val exploredTurn2 = tiles.exploredTurn(2, 2)
        val exploredTurn3 = tiles.exploredTurn(3, 3)

        assertTrue(tiles.isTileUnExplored(0, 0))
        assertFalse(tiles.isTileUnExplored(1, 1))
        assertFalse(tiles.isTileUnExplored(2, 2))
        assertFalse(tiles.isTileUnExplored(3, 3))
        assertTrue(exploredTurn1 < exploredTurn2)
        assertTrue(exploredTurn1 < exploredTurn3)
        assertTrue(exploredTurn2 < exploredTurn3)
    }

    @Test
    fun should_roll_and_init_last_roll_turn() {
        // given
        val tiles = PlayerExploredTiles(2, 2, Turn(20))
        tiles.init(0, 0, 10)

        // when
        tiles.setTileAsExplored(1, 1, Turn(35))

        // then
        assertEquals(1, tiles.exploredTurn(0, 0))
        assertEquals(95, tiles.exploredTurn(1, 1))
    }
}