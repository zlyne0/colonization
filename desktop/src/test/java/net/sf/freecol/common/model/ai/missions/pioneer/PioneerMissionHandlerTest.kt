package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ColonyAssert.assertThat
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.SettlementAssert
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.TileImprovement
import net.sf.freecol.common.model.TileImprovementType
import net.sf.freecol.common.model.TileImprovementType.PLOWED_IMPROVEMENT_TYPE_ID
import net.sf.freecol.common.model.UnitAssert.assertThat
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainerAssert.assertThat
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.findFirstMissionKt
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.colonyproduction.GoodsCollection
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

    @BeforeEach
    override fun setup() {
        super.setup()

        plowedType = Specification.instance.tileImprovementTypes.getById(PLOWED_IMPROVEMENT_TYPE_ID)
        roadType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID)
        clearForestType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.CLEAR_FOREST_IMPROVEMENT_TYPE_ID)

        clearAllMissions(dutch)
    }

    @Test
    fun `should create request goods mission for tools`() {
        // given
        removeImprovements(fortNassau, tileFrom(fortNassau, Direction.E))
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
        newTurnAndExecuteMission(dutch, 4)

        // then
        assertTrue(tileFrom(fortNassau, Direction.E).hasImprovementType(PLOWED_IMPROVEMENT_TYPE_ID))
        assertThat(pioneer).isAtLocation(fortNassau.tile)
            .hasRoleCount(0)

        val requestGoodsMissions = dutchMissionContainer.findMissions(RequestGoodsMission::class.java, {
            fortNassau.equalsId(it.colonyId) && pioneerMission.equalsId(it.purpose)
        })
        assertThat(requestGoodsMissions).hasSize(1)
        assertThat(requestGoodsMissions.get(0).amount(goodsType(GoodsType.TOOLS))).isEqualTo(100)
    }

    @Test
    fun `free colonists take tools from colony and improve tile`() {
        // given
        removeImprovements(fortNassau, tileFrom(fortNassau, Direction.E))
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
        newTurnAndExecuteMission(dutch, 2)

        // then
        assertThat(pioneer).isAtLocation(tileFrom(fortNassau, Direction.E))
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
        removeAllImprovements(nieuwAmsterdam)
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
        val transportRequest = dutchMissionContainer.findFirstMissionKt(TransportUnitRequestMission::class.java)
        assertThat(dutchMissionContainer)
            .hasDependMission(pioneerMission, transportRequest!!.id, TransportUnitRequestMission::class.java)
    }

    @Test
    fun `should deliver tools for pioneer goods request`() {
        // given
        removeAllImprovements(fortOranje)

        val player = dutch
        val playerMissionContainer = game.aiContainer.missionContainer(dutch)
        val seaTile = game.map.getTile(27, 80)

        val freeColonist = UnitFactory.create(UnitType.FREE_COLONIST, player, fortOranje.tile)
        val pioneerMission = PioneerMission(freeColonist, fortOranje)
        playerMissionContainer.addMission(pioneerMission)
        val requestGoodsMission = RequestGoodsMission(fortOranje, GoodsCollection.of(goodsType(GoodsType.TOOLS), 100), pioneerMission.id)
        playerMissionContainer.addMission(pioneerMission, requestGoodsMission)

        val ship = UnitFactory.create(UnitType.CARAVEL, player, seaTile)
        ship.goodsContainer.increaseGoodsQuantity(GoodsType.TOOLS, 100)
        val transportUnitMission = TransportUnitMission(ship)
        transportUnitMission.addCargoDest(fortOranje.tile, goodsType(GoodsType.TOOLS), 100, requestGoodsMission.id)
        playerMissionContainer.addMission(transportUnitMission)

        // when
        newTurnAndExecuteMission(player, 1)

        // then
        assertThat(transportUnitMission).isDone
        assertThat(requestGoodsMission).isDone
        assertThat(ship).hasNoGoods()
        assertThat(freeColonist)
            .isUnitRole(UnitRole.PIONEER)
            .hasRoleCount(unitRole(UnitRole.PIONEER).maximumCount)
    }

    fun addAllImprovements(player: Player) {
        val pathFinder = PathFinder()
        val pioneerMissionPlaner = PioneerMissionPlaner(game, pathFinder)
        var planScore = pioneerMissionPlaner.generateImprovementsPlanScore(player, AddImprovementPolicy.Balanced(player))
        for (objectScore in planScore) {
            for (improvement in objectScore.obj.improvements) {
                improvement.tile.addImprovement(TileImprovement(Game.idGenerator, improvement.improvementType))
            }
        }
        planScore = pioneerMissionPlaner.generateImprovementsPlanScore(player, AddImprovementPolicy.Balanced(player))
        for (objectScore in planScore) {
            for (improvement in objectScore.obj.improvements) {
                improvement.tile.addImprovement(TileImprovement(Game.idGenerator, improvement.improvementType))
            }
        }
    }

    fun givenImprovementsPlan() {
        val tileImprovementPlan = AddImprovementPolicy.Balanced(fortNassau.owner).generateImprovements(fortNassau)
        printToStdout(tileImprovementPlan)
        assertThat(tileImprovementPlan.improvements).hasSize(1)
        tileImprovementPlan.improvements.get(0).let { improvementPlan ->
            assertTrue(improvementPlan.tile.equalsCoordinates(tileFrom(fortNassau, Direction.E)))
            assertTrue(improvementPlan.improvementType.equalsId(plowedType))
        }
//        tileImprovementPlan.improvements.get(1).let { improvementPlan ->
//            assertTrue(improvementPlan.tile.equalsCoordinates(tileFrom(fortNassau, Direction.W)))
//            assertTrue(improvementPlan.improvementType.equalsId(plowedType))
//        }
    }

    private fun printToStdout(tileImprovementPlan: ColonyTilesImprovementPlan) {
        println("improvement.count: " + tileImprovementPlan.improvements.size)
        for (improvement in tileImprovementPlan.improvements) {
            println("improvementType: " + improvement.improvementType + ", tile: [" + improvement.tile.toStringCords() + "]")
        }
    }

    fun assertNoImprovementsPlan(colony: Colony) {
        assertEquals(0, AddImprovementPolicy.Balanced(colony.owner).generateImprovements(colony).improvements.size)
    }

    fun removeAllImprovements(colony: Colony) {
        for (colonyTile in colony.colonyTiles) {
            colonyTile.tile.removeTileImprovement(PLOWED_IMPROVEMENT_TYPE_ID)
            if (!colonyTile.tile.equalsCoordinates(colony.tile)) {
                colonyTile.tile.removeTileImprovement(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID)
            }
        }
    }

    fun removeImprovements(colony: Colony, tile: Tile) {
        for (colonyTile in colony.colonyTiles) {
            if (colonyTile.tile.equalsCoordinates(tile)) {
                colonyTile.tile.removeTileImprovement(PLOWED_IMPROVEMENT_TYPE_ID)
                if (!colonyTile.tile.equalsCoordinates(colony.tile)) {
                    colonyTile.tile.removeTileImprovement(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID)
                }
            }
        }
    }
}