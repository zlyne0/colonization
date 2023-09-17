package net.sf.freecol.common.model.map

import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.map.PowerInfluenceMap.Companion.FLOAT_UNKNOWN_VALUE
import net.sf.freecol.common.model.map.PowerInfluenceMap.Companion.INT_UNKNOWN_VALUE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.Savegame1600BaseClass

class PowerInfluenceMapTest: Savegame1600BaseClass() {

    @Test
    fun `should sum power from two source`() {
        // given
        val ALLOW_ONLY_LAND_TILES = { tile: Tile -> tile.type.isLand }
        val powerInfluenceMap = PowerInfluenceMap(game.map, ALLOW_ONLY_LAND_TILES, InfluenceRangeMapBuilder())

        val land1 = game.map.getTile(27, 75)
        val land2 = game.map.getTile(26, 70)

        // when
        powerInfluenceMap.addSourceLayer(land1, 5, 1f)
        powerInfluenceMap.addSourceLayer(land2, 5, 1f)

        // then
        // sum of two sources
        assertEquals(2f, powerInfluenceMap.powerSum(land1))
        assertEquals(6, powerInfluenceMap.powerProjectionRange(land1))
        assertEquals(2f, powerInfluenceMap.powerSum(land2))
        assertEquals(6, powerInfluenceMap.powerProjectionRange(land2))

        val landExample1 = game.map.getTile(24, 72)
        assertEquals(1f, powerInfluenceMap.powerSum(landExample1))
        assertEquals(2, powerInfluenceMap.powerProjectionRange(landExample1))
        val landExample2 = game.map.getTile(24, 77)
        assertEquals(1f, powerInfluenceMap.powerSum(landExample2))
        assertEquals(1, powerInfluenceMap.powerProjectionRange(landExample2))

        val waterTile = game.map.getTile(26, 69)
        assertEquals(FLOAT_UNKNOWN_VALUE, powerInfluenceMap.powerSum(waterTile))
        assertEquals(INT_UNKNOWN_VALUE, powerInfluenceMap.powerProjectionRange(waterTile))

        val outOfRange = game.map.getTile(23, 74)
        assertEquals(FLOAT_UNKNOWN_VALUE, powerInfluenceMap.powerSum(outOfRange))
        assertEquals(INT_UNKNOWN_VALUE, powerInfluenceMap.powerProjectionRange(outOfRange))
    }

}