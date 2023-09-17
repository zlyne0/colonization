package net.sf.freecol.common.model.map

import net.sf.freecol.common.model.Tile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.Savegame1600BaseClass

class InfluenceRangeMapTest : Savegame1600BaseClass() {

    @Test
    fun `should generate influence range map from builder`() {
        // given
        val builder = InfluenceRangeMapBuilder()

        // when
        builder.init(game.map, { tile: Tile -> tile.type.isLand })
        builder.addRangeSource(game.map.getTile(27, 75))
        builder.addRangeSource(game.map.getTile(26, 70))
        val influenceMap = builder.generateRange(5)
        // then

        assertThat(influenceMap.get(26, 70)).isEqualTo(0)
        assertThat(influenceMap.get(27, 75)).isEqualTo(0)
        assertThat(influenceMap.get(26, 71)).isEqualTo(1)
        assertThat(influenceMap.get(23, 74)).isEqualTo(5)
        assertThat(influenceMap.isUnknownValue(22, 74)).isTrue() // out of range
        assertThat(influenceMap.isUnknownValue(27, 71)).isTrue() // water
    }

    @Test
    fun `should generate power spread influence map`() {
        // given
        val builder = InfluenceRangeMapBuilder()

        // when
        builder.init(game.map, { tile: Tile -> tile.type.isLand })
        builder.addPowerSource(game.map.getTile(27, 75), 5)
        builder.addPowerSource(game.map.getTile(26, 70), 5)
        val influenceMap = builder.generateSpreadPower()
        // then

        assertThat(influenceMap.get(26, 70)).isEqualTo(5)
        assertThat(influenceMap.get(27, 75)).isEqualTo(5)
        assertThat(influenceMap.get(26, 71)).isEqualTo(4)
        assertThat(influenceMap.get(23, 73)).isEqualTo(1)
        assertThat(influenceMap.isUnknownValue(23, 74)).isTrue() // out of range
        assertThat(influenceMap.isUnknownValue(27, 71)).isTrue() // water
    }


}