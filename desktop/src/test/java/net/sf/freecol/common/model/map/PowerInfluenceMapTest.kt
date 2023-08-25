package net.sf.freecol.common.model.map

import net.sf.freecol.common.model.Tile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.Savegame1600BaseClass

class PowerInfluenceMapTest: Savegame1600BaseClass() {

    @Test
    fun `should sum power from two source`() {
        // given
        val ALLOW_ONLY_LAND_TILES = { tile: Tile -> tile.type.isLand }
        val powerInfluenceMap = PowerInfluenceMap(game.map, 1, ALLOW_ONLY_LAND_TILES)

        val land1 = game.map.getTile(27, 75)
        val land2 = game.map.getTile(26, 70)

        // when
        powerInfluenceMap.addSource(land1, 5)
        powerInfluenceMap.addSource(land2, 5)

        // then
        val waterTile = game.map.getTile(28, 74)

        assertThat(powerInfluenceMap.power(waterTile)).isEqualTo(powerInfluenceMap.resetValue)
        assertThat(powerInfluenceMap.power(game.map.getTile(22, 75))).isEqualTo(0)
        assertThat(powerInfluenceMap.power(game.map.getTile(26, 73))).isEqualTo(6)
    }

}