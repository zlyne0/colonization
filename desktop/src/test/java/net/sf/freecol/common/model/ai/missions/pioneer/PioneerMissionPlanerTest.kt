package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.TileImprovement
import net.sf.freecol.common.model.TileImprovementType
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitAssert.assertThat
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.map.path.PathFinder
import net.sf.freecol.common.model.specification.GoodsType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import promitech.colonization.ColAssertDefinition.Companion.assertThat
import promitech.colonization.Direction
import promitech.colonization.ai.score.ScoreableObjectsListAssert
import promitech.colonization.savegame.Savegame1600BaseClass
import java.util.function.Predicate

internal class PioneerMissionPlanerTest : Savegame1600BaseClass() {

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
        val balanced = AddImprovementPolicy.Balanced(fortNassau.owner)
        val tileImprovementPlan = balanced.generateImprovements(fortNassau)

        // then
        assertEquals(3, tileImprovementPlan.improvements.size)
        contains(tileImprovementPlan, tileFrom(fortNassau, Direction.NW), roadType)
        contains(tileImprovementPlan, tileFrom(fortNassau, Direction.E), plowedType)
        contains(tileImprovementPlan, tileFrom(fortNassau, Direction.NE), roadType)
    }

    @Test
    fun `should generate improvements for Fort Maurits`() {
        // given
        removeAllImprovements(fortMaurits)

        // when
        val balanced = AddImprovementPolicy.Balanced(fortMaurits.owner)
        val tileImprovementPlan = balanced.generateImprovements(fortMaurits)

        // then
        assertEquals(2, tileImprovementPlan.improvements.size)
        contains(tileImprovementPlan, tileFrom(fortMaurits, Direction.E), roadType)
        contains(tileImprovementPlan, tileFrom(fortMaurits, Direction.SE), plowedType)
    }

    @Test
    fun `should generate no improvements for Fort Maurits when all created`() {
        // given
        removeAllImprovements(fortMaurits)
        tileFrom(fortMaurits, Direction.E).addImprovement(TileImprovement(Game.idGenerator, roadType))
        tileFrom(fortMaurits, Direction.SE).addImprovement(TileImprovement(Game.idGenerator, plowedType))
        tileFrom(fortMaurits, Direction.S).addImprovement(TileImprovement(Game.idGenerator, plowedType))

        // when
        val balanced = AddImprovementPolicy.Balanced(fortMaurits.owner)
        val tileImprovementPlan = balanced.generateImprovements(fortMaurits)

        // then
        assertEquals(0, tileImprovementPlan.improvements.size)
    }

    @Test
    fun `can generate improvement plan score`() {
        // given
        val pathFinder = PathFinder()
        val pioneerMissionPlaner = PioneerMissionPlaner(game, pathFinder)
        val policy = AddImprovementPolicy.Balanced(dutch)

        // when
        val planScore = pioneerMissionPlaner.generateImprovementsPlanScore(dutch, policy)

        // then
        ScoreableObjectsListAssert.assertThat(planScore)
            .hasSize(4)
            .hasScore(0, 50, eq(fortNassau))
            .hasScore(1, 0, eq(fortMaurits))
            .hasScore(2, 0, eq(fortOranje))
            .hasScore(3, 0, eq(nieuwAmsterdam))
    }

    @Test
    fun `should create buy pioneer and create mission`() {
        // given
        removeAllImprovements(nieuwAmsterdam)
        val pathFinder = PathFinder()
        val pioneerMissionPlaner = PioneerMissionPlaner(game, pathFinder)
        val playerMissionContainer = game.aiContainer.missionContainer(dutch)
        playerMissionContainer.clearAllMissions()

        // when
        val buyPlan = pioneerMissionPlaner.createBuyPlan(dutch, playerMissionContainer)
        if (buyPlan != null) {
            pioneerMissionPlaner.handlePioneerBuyPlan(buyPlan, playerMissionContainer)
        }

        // then
        val findMissions = playerMissionContainer.findMissions(PioneerMission::class.java)
        assertThat(findMissions).hasSize(1)
        val mission = findMissions[0]

        assertThat(mission.isToColony(nieuwAmsterdam)).isTrue()
        assertThat(mission.pioneer).isAtLocation(dutch.europe)
    }

    @Test
    fun `should generate buy hardy pioneer specialist`() {
        // given
        val pathFinder = PathFinder()
        val pioneerMissionPlaner = PioneerMissionPlaner(game, pathFinder)
        val player = dutch
        setGold(player, unitType(UnitType.HARDY_PIONEER).price * 2)

        // when
        val buyPionnierOrder = pioneerMissionPlaner.calculateBuyPioneerOrder(player, false)
        var pioneer: Unit? = null
        if (buyPionnierOrder is BuyPioneerOrder.BuySpecialistOrder) {
            pioneer = buyPionnierOrder.buy(player)
        }

        // then
        assertThat(pioneer).isNotNull
        assertThat(pioneer)
            .isAtLocation(player.europe)
            .isUnitType(UnitType.HARDY_PIONEER)
            .isUnitRole(UnitRole.PIONEER)
    }

    @Test
    fun `should generate buy colonist pioneer`() {
        // given
        val pathFinder = PathFinder()
        val pioneerMissionPlaner = PioneerMissionPlaner(game, pathFinder)
        val player = dutch
        setGold(player, 2000)

        // when
        val buyPionnierOrder = pioneerMissionPlaner.calculateBuyPioneerOrder(player, false)
        var pioneer: Unit? = null
        if (buyPionnierOrder is BuyPioneerOrder.RecruitColonistOrder) {
            pioneer = buyPionnierOrder.buy(player, game)
        }

        // then
        assertThat(pioneer).isNotNull
        assertThat(pioneer)
            .isAtLocation(player.europe)
            .isUnitType(UnitType.FREE_COLONIST)
            .isUnitRole(UnitRole.PIONEER)
    }

    @Test
    fun `when no gold should generate can not afford buy pioneer order`() {
        // given
        val pathFinder = PathFinder()
        val pioneerMissionPlaner = PioneerMissionPlaner(game, pathFinder)
        val player = dutch
        setGold(player, 10)

        // when
        val buyPioneerOrder = pioneerMissionPlaner.calculateBuyPioneerOrder(player, false)

        // then
        assertThat(buyPioneerOrder).isInstanceOf(BuyPioneerOrder.CanNotAfford::class.java)
    }

    @Test
    fun `should find next destination to improve colony`() {
        // given
        val playerMissionContainer = game.aiContainer.missionContainer(dutch)
        playerMissionContainer.clearAllMissions()

        val betweenColoniesTile = game.map.getTile(22, 78)
        playerMissionContainer.addMission(
            PioneerMission(
                UnitFactory.create(UnitType.HARDY_PIONEER, UnitRole.PIONEER, dutch, betweenColoniesTile),
                nieuwAmsterdam
            )
        )
        val pioneerMission = PioneerMission(
            UnitFactory.create(UnitType.HARDY_PIONEER, UnitRole.PIONEER, dutch, betweenColoniesTile),
            fortMaurits
        )

        val pioneerMissionPlaner = PioneerMissionPlaner(game, PathFinder())
        // when
        val improveDestination = pioneerMissionPlaner.findNextColonyToImprove(
            pioneerMission.pioneer,
            pioneerMission.colonyId,
            playerMissionContainer
        )

        // then
        assertThat(improveDestination)
            .isNotNull()
            .isInstanceOf(PioneerDestination.TheSameIsland::class.java)
        if (improveDestination is PioneerDestination.TheSameIsland) {
            assertThat(improveDestination.plan.colony.id).isEqualTo(fortNassau.id)
        }
    }

    @Test
    fun `should find colony hardy pioneer worker`() {
        // given
        val player = dutch
        val playerMissionContainer = game.aiContainer.missionContainer(player)
        playerMissionContainer.clearAllMissions()

        val workerFreeColonist = dutch.units.getById("unit:6436")
        assertThat(fortOranje.units.containsId(workerFreeColonist)).isTrue()
        workerFreeColonist.changeUnitType(unitType(UnitType.HARDY_PIONEER))

        val pioneerMissionPlaner = PioneerMissionPlaner(game, PathFinder())

        // when
        val colonyHardyPioneer = pioneerMissionPlaner.findColonyHardyPioneerInRange(player, nieuwAmsterdam.tile)

        // then
        assertThat {
            colonyHardyPioneer.isNotNull()
            colonyHardyPioneer!!.colony eqId fortOranje
            colonyHardyPioneer.hardyPioneer eqId workerFreeColonist
        }
    }

    @Test
    fun `should find colony hardy pioneer worker by the sea`() {
        // given
        val player = dutch
        val playerMissionContainer = game.aiContainer.missionContainer(player)
        playerMissionContainer.clearAllMissions()

        val workerFreeColonist = dutch.units.getById("unit:6436")
        assertThat(fortOranje.units.containsId(workerFreeColonist)).isTrue()
        workerFreeColonist.changeUnitType(unitType(UnitType.HARDY_PIONEER))

        val pioneerMissionPlaner = PioneerMissionPlaner(game, PathFinder())

        // when
        val colonyHardyPioneer = pioneerMissionPlaner.findColonyHardyPioneerInRange(player, islandTile)

        // then

        assertThat {
            colonyHardyPioneer.isNotNull()
            colonyHardyPioneer!!.colony eqId fortOranje
            colonyHardyPioneer.hardyPioneer eqId workerFreeColonist
        }
    }

    @Test
    fun `should find colony to equipt pioneer with tools`() {
        // given
        val player = dutch
        val playerAiContainer = game.aiContainer.playerAiContainer(player)
        playerAiContainer.colonySupplyGoods.clear()

        val playerMissionContainer = game.aiContainer.missionContainer(player)
        playerMissionContainer.clearAllMissions()

        fortOranje.goodsContainer.increaseGoodsQuantity(GoodsType.TOOLS, 100)
        val pioneerMissionPlaner = PioneerMissionPlaner(game, PathFinder())

        // when
        val colonyToEquiptPioneer = pioneerMissionPlaner.findColonyToEquiptPioneerInRange(player, nieuwAmsterdam.tile, null)

        // then
        assertThat {
            colonyToEquiptPioneer.isNotNull()
            colonyToEquiptPioneer!! eqId fortOranje
        }
    }

    @Test
    fun `should find colony to equipt pioneer with tools by sea path`() {
        // given
        val player = dutch
        val playerAiContainer = game.aiContainer.playerAiContainer(player)
        playerAiContainer.colonySupplyGoods.clear()

        val playerMissionContainer = game.aiContainer.missionContainer(player)
        playerMissionContainer.clearAllMissions()

        fortOranje.goodsContainer.increaseGoodsQuantity(GoodsType.TOOLS, 100)
        val pioneerMissionPlaner = PioneerMissionPlaner(game, PathFinder())

        // when
        val colonyToEquiptPioneer = pioneerMissionPlaner.findColonyToEquiptPioneerInRange(player, islandTile, null)

        // then
        assertThat {
            colonyToEquiptPioneer.isNotNull()
            colonyToEquiptPioneer!! eqId fortOranje
        }
    }

    fun eq(colony: Colony): Predicate<ColonyTilesImprovementPlan> {
        return Predicate<ColonyTilesImprovementPlan> { plan -> plan.colony.equalsId(colony) }
    }

    private fun contains(tileImprovementPlan: ColonyTilesImprovementPlan, tile: Tile, improvementType: TileImprovementType) {
        for (improvementPlan in tileImprovementPlan.improvements) {
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