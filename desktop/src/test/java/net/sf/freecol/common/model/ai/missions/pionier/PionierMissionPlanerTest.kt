package net.sf.freecol.common.model.ai.missions.pionier

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.TileAssert.*
import net.sf.freecol.common.model.TileImprovement
import net.sf.freecol.common.model.TileImprovementType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import promitech.colonization.Direction
import promitech.colonization.savegame.Savegame1600BaseClass

internal class PionierMissionPlanerTest : Savegame1600BaseClass() {

    lateinit var plowedType : TileImprovementType
    lateinit var roadType : TileImprovementType
    lateinit var clearForestType : TileImprovementType

    @BeforeEach
    override fun setup() {
        super.setup()

        plowedType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.PLOWED_IMPROVEMENT_TYPE_ID)
        roadType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID)
        clearForestType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.CLEAR_FOREST_IMPROVEMENT_TYPE_ID)
    }

    @Test
    fun `should generate improvements for Fort Nassau`() {
        // given
        removeAllImprovements(fortNassau)

        // when
        val balanced = AddImprovementPolicy.Balanced()
        val tileImprovementPlan = balanced.generateImprovements(fortNassau)

        // then
        assertEquals(5, tileImprovementPlan.size)
        contains(tileImprovementPlan, tileFrom(fortNassau, Direction.NW), roadType)
        contains(tileImprovementPlan, tileFrom(fortNassau, Direction.W), plowedType)
        contains(tileImprovementPlan, tileFrom(fortNassau, Direction.E), plowedType)
        contains(tileImprovementPlan, tileFrom(fortNassau, Direction.NE), roadType)
        contains(tileImprovementPlan, fortNassau.tile, clearForestType)
    }

    @Test
    fun `should generate improvements for Fort Maurits`() {
        // given
        removeAllImprovements(fortMaurits)

        // when
        val balanced = AddImprovementPolicy.Balanced()
        val tileImprovementPlan = balanced.generateImprovements(fortMaurits)

        // then
        assertEquals(3, tileImprovementPlan.size)
        contains(tileImprovementPlan, tileFrom(fortMaurits, Direction.E), roadType)
        contains(tileImprovementPlan, tileFrom(fortMaurits, Direction.SE), plowedType)
        contains(tileImprovementPlan, tileFrom(fortMaurits, Direction.S), plowedType)
    }

    @Test
    fun `should generate no improvements for Fort Maurits when all created`() {
        // given
        removeAllImprovements(fortMaurits)
        tileFrom(fortMaurits, Direction.E).addImprovement(TileImprovement(Game.idGenerator, roadType))
        tileFrom(fortMaurits, Direction.SE).addImprovement(TileImprovement(Game.idGenerator, plowedType))
        tileFrom(fortMaurits, Direction.S).addImprovement(TileImprovement(Game.idGenerator, plowedType))

        // when
        val balanced = AddImprovementPolicy.Balanced()
        val tileImprovementPlan = balanced.generateImprovements(fortMaurits)

        // then
        assertEquals(0, tileImprovementPlan.size)
    }

    private fun contains(tileImprovementPlan: List<TileImprovementPlan>, tile: Tile, improvementType: TileImprovementType) {
        for (improvementPlan in tileImprovementPlan) {
            if (improvementPlan.tile.equalsCoordinates(tile) && improvementPlan.improvementType.equalsId(improvementType)) {
                return
            }
        }
        Assertions.fail<TileImprovementPlan>(String.format(
            "can not find improvement type %s on tile [%s]", improvementType.toSmallIdStr(), tile.toStringCords()
        ))
    }

    fun removeAllImprovements(colony: Colony) {
        for (colonyTile in colony.colonyTiles) {
            colonyTile.tile.removeTileImprovement(TileImprovementType.PLOWED_IMPROVEMENT_TYPE_ID)
            if (!colonyTile.tile.equalsCoordinates(colony.tile)) {
                colonyTile.tile.removeTileImprovement(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID)
            }
        }
    }
}