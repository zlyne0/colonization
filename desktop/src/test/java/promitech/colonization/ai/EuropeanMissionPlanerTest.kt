package promitech.colonization.ai

import net.sf.freecol.common.model.Tile
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitAssert.assertThat
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainerAssert.assertThat
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EuropeanMissionPlanerTest : MissionHandlerBaseTestClass() {

//    var sourceTile: Tile? = null
//    var disembarkTile: Tile? = null
    lateinit var fortOrangeTile: Tile
    lateinit var galleon: Unit
    lateinit var u1: Unit
    lateinit var u2: Unit

    @BeforeEach
    override fun setup() {
        super.setup()

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

        val planer = EuropeanMissionPlaner(game, di.pathFinder, di.pathFinder2)
        planer.setAvoidPurchasesAndCollectGold()

        // when
        planer.transportUnitFromEurope(galleon, missionContainer)

        // then
        assertThat(missionContainer).hasMission(TransportUnitMission::class.java, 1)

        val transportUnitMission = missionContainer.findFirstMission(TransportUnitMission::class.java)
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

        val planer = EuropeanMissionPlaner(game, di.pathFinder, di.pathFinder2)

        // when
        planer.navyUnitPlaner(galleon, missionContainer)

        // then
        assertThat(missionContainer).hasMission(TransportUnitMission::class.java, 1)

        val transportUnitMission = missionContainer.findFirstMission(TransportUnitMission::class.java)
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