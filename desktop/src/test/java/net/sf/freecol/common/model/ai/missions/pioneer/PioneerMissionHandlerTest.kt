package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyAssert.assertThat
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.SettlementAssert
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.TileAssert.assertThat
import net.sf.freecol.common.model.TileImprovement
import net.sf.freecol.common.model.TileImprovementType
import net.sf.freecol.common.model.UnitAssert.assertThat
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainerAssert.assertThat
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.GoodsType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import promitech.colonization.Direction
import promitech.colonization.savegame.AbstractMissionAssert.assertThat

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

        // first improvement center tile
        // clear forest and plow on center
        newTurnAndExecuteMission(dutch, 4)
        assertFalse(fortNassau.tile.type.isForested)
        newTurnAndExecuteMission(dutch, 3)
        assertThat(fortNassau.tile).hasImprovement(plowedType)

        // second improvement on west
        // move and wait, plow
        newTurnAndExecuteMission(dutch, 3)
        //
        //newTurnAndExecuteMission(dutch, 3)

        // then
        assertNoImprovementsPlan(fortNassau)
        assertThat(tileFrom(fortNassau, Direction.W)).hasImprovement(plowedType)
        assertThat(pioneer).isAtLocation(game.map.getSafeTile(22,78))
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
        // move and start improve
        newTurnAndExecuteMission(dutch, 1)
        // clear
        newTurnAndExecuteMission(dutch, 3)

        // then
        assertThat(pioneer).isAtLocation(fortNassau.tile)
            .hasRoleCount(0)
        assertFalse(fortNassau.tile.type.isForested)

        val requestGoodsMissions = dutchMissionContainer.findMissions(RequestGoodsMission::class.java, {
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

    @Test
    fun `should back to colony and resolve pionner unit`() {
        // given
        addAllImprovements(dutch)
        fortNassau.goodsContainer.decreaseAllToZero()

        val dutchMissionContainer = game.aiContainer.missionContainer(dutch)
        val pioneer = UnitFactory.create(UnitType.FREE_COLONIST, UnitRole.PIONEER, dutch, tileFrom(fortNassau, Direction.W))
        val pioneerMission = PioneerMission(pioneer, fortNassau)
        dutchMissionContainer.addMission(pioneerMission)

        // when
        // back to colony
        newTurnAndExecuteMission(dutch, 1)

        // then
        assertThat(pioneerMission).isDone()
        assertThat(pioneer)
            .isUnitRole(UnitRole.DEFAULT_ROLE_ID)
            .isAtLocation(fortNassau.tile)
        assertThat(fortNassau).hasGoods(GoodsType.TOOLS, 100)
    }

    @Test
    fun `should change colony after all improvements`() {
        // given
        val dutchMissionContainer = game.aiContainer.missionContainer(dutch)
        val pioneer = UnitFactory.create(UnitType.HARDY_PIONEER, UnitRole.PIONEER, dutch, tileFrom(fortOranje, Direction.W))
        val pioneerMission = PioneerMission(pioneer, fortOranje)
        dutchMissionContainer.addMission(pioneerMission)

        // when
        newTurnAndExecuteMission(dutch, 1)

        // then
        assertTrue(pioneerMission.isToColony(nieuwAmsterdam))
    }

    @Test
    fun `should generate transport request mission when move to another island`() {
        // given
        val dutchMissionContainer = game.aiContainer.missionContainer(dutch)

        val islandTile = game.map.getTile(25, 86)

        val pioneer = UnitFactory.create(UnitType.HARDY_PIONEER, UnitRole.PIONEER, dutch, islandTile)
        val pioneerMission = PioneerMission(pioneer, fortOranje)
        dutchMissionContainer.addMission(pioneerMission)

        // when
        newTurnAndExecuteMission(dutch, 1)

        // then
        val transportRequest = dutchMissionContainer.findFirstMission(TransportUnitRequestMission::class.java)
        assertThat(dutchMissionContainer)
            .hasDependMission(pioneerMission, transportRequest.id, TransportUnitRequestMission::class.java)
    }

    fun addAllImprovements(player: Player) {
        val pathFinder = PathFinder()
        val pioneerMissionPlaner = PioneerMissionPlaner(game, pathFinder)
        var planScore = pioneerMissionPlaner.generateImprovementsPlanScore(player, AddImprovementPolicy.Balanced())
        for (objectScore in planScore) {
            for (improvement in objectScore.obj.improvements) {
                improvement.tile.addImprovement(TileImprovement(Game.idGenerator, improvement.improvementType))
            }
        }
        planScore = pioneerMissionPlaner.generateImprovementsPlanScore(player, AddImprovementPolicy.Balanced())
        for (objectScore in planScore) {
            for (improvement in objectScore.obj.improvements) {
                improvement.tile.addImprovement(TileImprovement(Game.idGenerator, improvement.improvementType))
            }
        }
    }

    fun givenImprovementsPlan() {
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