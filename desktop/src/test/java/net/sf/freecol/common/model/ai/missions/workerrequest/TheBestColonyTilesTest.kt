package net.sf.freecol.common.model.ai.missions.workerrequest

import net.sf.freecol.common.model.Tile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import promitech.colonization.ColAssertDefinition.Companion.assertThat
import promitech.colonization.savegame.Savegame1600BaseClass

class TheBestColonyTilesTest  : Savegame1600BaseClass() {

    lateinit var tile1: Tile
    lateinit var tile2: Tile
    lateinit var tile3: Tile
    lateinit var tile4: Tile

    @BeforeEach
    override fun setup() {
        super.setup()

        tile1 = game.map.getSafeTile(10, 10)
        tile2 = game.map.getSafeTile(10, 11)
        tile3 = game.map.getSafeTile(10, 12)
        tile4 = game.map.getSafeTile(10, 13)
    }

    @Test
    fun `should sort tiles by weight`() {
        // given
        val theBestColonyTiles = TheBestColonyTiles()

        // when
        theBestColonyTiles.add(tile1, 0, 10)
        theBestColonyTiles.add(tile2, 0, 12)
        theBestColonyTiles.add(tile3, 0, 13)
        theBestColonyTiles.add(tile4, 0, 14)

        // then
        val tilesInTurns = theBestColonyTiles.tilesInTurns(0)
        assertThat(tilesInTurns.size).isEqualTo(3)
        assertThat(tilesInTurns.get(0).tile).isEqualTo(tile4)
        assertThat(tilesInTurns.get(0).weight).isEqualTo(14)
        assertThat(tilesInTurns.get(1).tile).isEqualTo(tile3)
        assertThat(tilesInTurns.get(1).weight).isEqualTo(13)
        assertThat(tilesInTurns.get(2).tile).isEqualTo(tile2)
        assertThat(tilesInTurns.get(2).weight).isEqualTo(12)
    }

    @Test
    fun `should group tiles by turn range`() {
        // given
        val theBestColonyTiles = TheBestColonyTiles()

        // when
        theBestColonyTiles.add(tile1, 0, 10)
        theBestColonyTiles.add(tile2, 0, 12)
        theBestColonyTiles.add(tile3, 1, 13)
        theBestColonyTiles.add(tile4, 2, 14)

        // then
        assertThat {
            theBestColonyTiles.tilesInTurns(0).size eq 2
            theBestColonyTiles.tilesInTurns(0).get(0).tile eqId tile2
            theBestColonyTiles.tilesInTurns(0).get(1).tile eqId tile1
            theBestColonyTiles.tilesInTurns(1).size eq 1
            theBestColonyTiles.tilesInTurns(1).get(0).tile eqId tile3
            theBestColonyTiles.tilesInTurns(2).size eq 1
            theBestColonyTiles.tilesInTurns(2).get(0).tile eqId tile4
        }
    }

}