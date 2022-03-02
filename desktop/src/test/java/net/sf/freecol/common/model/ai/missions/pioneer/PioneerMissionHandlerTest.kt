package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyAssert
import net.sf.freecol.common.model.SettlementAssert
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.TileAssert.*
import net.sf.freecol.common.model.TileImprovementType
import net.sf.freecol.common.model.UnitAssert.*
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.ai.missions.findRecursively
import net.sf.freecol.common.model.specification.GoodsType
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import promitech.colonization.Direction

class PioneerMissionHandlerTest : MissionHandlerBaseTestClass() {

    lateinit var plowedType : TileImprovementType
    lateinit var roadType : TileImprovementType
    lateinit var clearForestType : TileImprovementType
    lateinit var balancedImprovementPolicy : AddImprovementPolicy

    @BeforeEach
    override fun setup() {
        super.setup()

        plowedType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.PLOWED_IMPROVEMENT_TYPE_ID)
        roadType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID)
        clearForestType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.CLEAR_FOREST_IMPROVEMENT_TYPE_ID)
        balancedImprovementPolicy = AddImprovementPolicy.Balanced()

        clearAllMissions(dutch)
    }

    @Test
    fun `should improve two tiles`() {
        // given
        givenImprovementsPlan()

        val dutchMissionContainer = game.aiContainer.missionContainer(dutch)
        val pioneer = UnitFactory.create(UnitType.HARDY_PIONEER, UnitRole.PIONEER, dutch, fortNassau.tile)
        dutchMissionContainer.addMission(PioneerMission(pioneer, fortNassau))

        // when
        // clear forest
        newTurnAndExecuteMission(dutch, 3)
        // plow
        newTurnAndExecuteMission(dutch, 3)
        // move
        newTurnAndExecuteMission(dutch, 1)
        // plow
        newTurnAndExecuteMission(dutch, 4)
        // move
        newTurnAndExecuteMission(dutch, 1)

        // then
        assertThat(pioneer).isAtLocation(tileFrom(fortNassau, Direction.W))
        assertNoImprovementsPlan(fortNassau)
        assertThat(tileFrom(fortNassau, Direction.W)).hasImprovement(plowedType)
        assertThat(fortNassau.tile).hasImprovement(plowedType)
    }

    @Test
    fun `should create request goods mission for tools`() {
        // given
        givenImprovementsPlan()

        val dutchMissionContainer = game.aiContainer.missionContainer(dutch)
        val pioneer = UnitFactory.create(UnitType.HARDY_PIONEER, UnitRole.PIONEER, dutch, tileFrom(fortNassau, Direction.W))
        pioneer.changeRole(unitRole(UnitRole.PIONEER), 1)
        val pioneerMission = PioneerMission(pioneer, fortNassau)
        dutchMissionContainer.addMission(pioneerMission)

        // when
        // move
        newTurnAndExecuteMission(dutch, 1)
        // clear
        newTurnAndExecuteMission(dutch, 4)

        // then
        assertThat(pioneer).isAtLocation(fortNassau.tile)
            .hasRoleCount(0)
        assertFalse(fortNassau.tile.type.isForested)

        val requestGoodsMissions = dutchMissionContainer.findRecursively(RequestGoodsMission::class.java, {
            fortNassau.equalsId(it.colonyId) && pioneerMission.equalsId(it.purpose)
        })
        assertThat(requestGoodsMissions).hasSize(1)
        assertThat(requestGoodsMissions.get(0).amount(goodsType(GoodsType.TOOLS))).isEqualTo(100)
    }

    @Test
    fun `free colonists take tools from colony and improve tile`() {
        // given
        givenImprovementsPlan()
        fortNassau.goodsContainer.decreaseAllToZero()

        val dutchMissionContainer = game.aiContainer.missionContainer(dutch)
        val pioneer = UnitFactory.create(UnitType.HARDY_PIONEER, UnitRole.DEFAULT_ROLE_ID, dutch, tileFrom(fortNassau, Direction.W))
        dutchMissionContainer.addMission(PioneerMission(pioneer, fortNassau))

        fortNassau.goodsContainer.increaseGoodsQuantity(GoodsType.TOOLS, 100)

        // when
        // back to colony and take tools
        newTurnAndExecuteMission(dutch, 1)
        // start improve
        newTurnAndExecuteMission(dutch, 1)

        // then
        assertThat(pioneer).isAtLocation(fortNassau.tile)
            .isImproveming()
        SettlementAssert.assertThat(fortNassau).hasGoods(GoodsType.TOOLS, 0)
    }

    private fun givenImprovementsPlan() {
        val tileImprovementPlan = balancedImprovementPolicy.generateImprovements(fortNassau)
        tileImprovementPlan.improvements.get(0).let { improvementPlan ->
            assertTrue(improvementPlan.tile.equalsCoordinates(fortNassau.tile))
            assertTrue(improvementPlan.improvementType.equalsId(clearForestType))
        }
        tileImprovementPlan.improvements.get(1).let { improvementPlan ->
            assertTrue(improvementPlan.tile.equalsCoordinates(tileFrom(fortNassau, Direction.W)))
            assertTrue(improvementPlan.improvementType.equalsId(plowedType))
        }
    }

    fun assertNoImprovementsPlan(colony: Colony) {
        assertEquals(0, balancedImprovementPolicy.generateImprovements(colony).improvements.size)
    }
}