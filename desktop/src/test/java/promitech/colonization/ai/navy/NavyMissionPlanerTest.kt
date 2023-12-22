package promitech.colonization.ai.navy

import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitAssert.assertThat
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainerAssert
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.findFirstMissionKt
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMissionPlaner
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMissionPlaner
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import promitech.colonization.ai.NavyMissionPlaner
import promitech.colonization.ai.military.DefencePlaner
import promitech.colonization.ai.purchase.PurchasePlaner

class NavyMissionPlanerTest : MissionHandlerBaseTestClass() {

    lateinit var fortOrangeTile: Tile
    lateinit var galleon: Unit
    lateinit var u1: Unit
    lateinit var u2: Unit

    lateinit var planer: NavyMissionPlaner

    @BeforeEach
    override fun setup() {
        super.setup()

        val purchasePlaner = PurchasePlaner(game)
        purchasePlaner.avoidPurchasesAndCollectGold()
        planer = NavyMissionPlaner(
            purchasePlaner,
            ColonyWorkerRequestPlaner(
                game,
                di.pathFinder,
                PioneerMissionPlaner(game, di.pathFinder),
                DefencePlaner(game, di.pathFinder)
            ),
            TransportGoodsToSellMissionPlaner(game, di.pathFinder)
        )

        clearAllMissions(dutch)
    }

    @Test
    fun `should create transport unit mission from request transport unit mission when carrier is at europe`() {
        // given
        val missionContainer = game.aiContainer.missionContainer(dutch)

        fortOrangeTile = fortOranje.tile
        galleon = UnitFactory.create(UnitType.GALLEON, dutch, dutch.getEurope())
        u1 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, dutch.getEurope())
        u2 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, dutch.getEurope())

        val transportRequest1 = TransportUnitRequestMission(game.turn, u1, fortOrangeTile)
        missionContainer.addMission(transportRequest1)
        val transportRequest2 = TransportUnitRequestMission(game.turn, u2, fortOrangeTile)
        missionContainer.addMission(transportRequest2)

        // when
        planer.transportFromEurope(galleon, missionContainer)

        // then
        PlayerMissionsContainerAssert.assertThat(missionContainer).hasMission(TransportUnitMission::class.java, 1)

        val transportUnitMission = missionContainer.findFirstMissionKt(TransportUnitMission::class.java)!!
        assertThat(transportUnitMission.carrier).isEqualsTo(galleon)
        assertThat(transportUnitMission.unitsDest).hasSize(2)

        assertThat(transportUnitMission.unitsDest.get(0).unit.equalsId(u1)).isTrue()
        assertThat(transportUnitMission.unitsDest.get(0).dest.equalsCoordinates(fortOrangeTile)).isTrue()
        assertThat(transportUnitMission.unitsDest.get(0).transportRequestMissionId.equals(transportRequest1.id)).isTrue()
        assertThat(transportUnitMission.unitsDest.get(1).unit.equalsId(u2)).isTrue()
        assertThat(transportUnitMission.unitsDest.get(1).dest.equalsCoordinates(fortOrangeTile)).isTrue()
        assertThat(transportUnitMission.unitsDest.get(1).transportRequestMissionId.equals(transportRequest2.id)).isTrue()

        assertThat(transportRequest1.transportUnitMissionId).isEqualTo(transportUnitMission.id)
        assertThat(transportRequest2.transportUnitMissionId).isEqualTo(transportUnitMission.id)
    }

    @Test
    fun `should create transport unit mission from request transport unit mission when carrier is at new world`() {
        // given
        // clear because mission planer prioritize goods sell transport then unit move
        for (settlement in dutch.settlements) {
            settlement.goodsContainer.clear()
        }
        val missionContainer = game.aiContainer.missionContainer(dutch)

        val islandTile = game.map.getSafeTile(25, 86)
        val notFarAwaySeaTile = game.map.getSafeTile(28, 84)
        fortOrangeTile = fortOranje.tile

        galleon = UnitFactory.create(UnitType.GALLEON, dutch, notFarAwaySeaTile)
        u1 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, islandTile)
        u2 = UnitFactory.create(UnitType.FREE_COLONIST, dutch, islandTile)

        val transportRequest1 = TransportUnitRequestMission(game.turn, u1, fortOrangeTile)
        missionContainer.addMission(transportRequest1)
        val transportRequest2 = TransportUnitRequestMission(game.turn, u2, fortOrangeTile)
        missionContainer.addMission(transportRequest2)

        // when
        planer.plan(galleon, missionContainer)

        // then
        PlayerMissionsContainerAssert.assertThat(missionContainer).hasMission(TransportUnitMission::class.java, 1)

        val transportUnitMission = missionContainer.findFirstMissionKt(TransportUnitMission::class.java)!!
        assertThat(transportUnitMission.carrier).isEqualsTo(galleon)

        assertThat(transportUnitMission.unitsDest).hasSize(2)
        assertThat(transportUnitMission.unitsDest.get(0).unit.equalsId(u1)).isTrue()
        assertThat(transportUnitMission.unitsDest.get(0).dest.equalsCoordinates(fortOrangeTile)).isTrue()
        assertThat(transportUnitMission.unitsDest.get(0).transportRequestMissionId.equals(transportRequest1.id)).isTrue()
        assertThat(transportUnitMission.unitsDest.get(1).unit.equalsId(u2)).isTrue()
        assertThat(transportUnitMission.unitsDest.get(1).dest.equalsCoordinates(fortOrangeTile)).isTrue()
        assertThat(transportUnitMission.unitsDest.get(1).transportRequestMissionId.equals(transportRequest2.id)).isTrue()

        assertThat(transportRequest1.transportUnitMissionId).isEqualTo(transportUnitMission.id)
        assertThat(transportRequest2.transportUnitMissionId).isEqualTo(transportUnitMission.id)
    }
}