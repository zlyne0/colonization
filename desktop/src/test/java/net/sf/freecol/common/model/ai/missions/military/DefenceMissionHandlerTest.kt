package net.sf.freecol.common.model.ai.missions.military

import net.sf.freecol.common.model.TileAssert
import net.sf.freecol.common.model.Unit
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainerAssert
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.AbstractMissionAssert.assertThat

class DefenceMissionHandlerTest : MissionHandlerBaseTestClass() {

    @BeforeEach
    override fun setup() {
        super.setup()
        game.aiContainer.missionContainer(dutch).clearAllMissions()
    }

    lateinit var galleon: Unit
    lateinit var u1: Unit
    lateinit var u2: Unit

    @Test
    fun `should create transport request when on carrier and it does not exist`() {
        // given
        val missionContainer = game.aiContainer.missionContainer(dutch)
        val farFromFortOrange = game.map.getTile(29, 89)

        galleon = UnitFactory.create(UnitType.GALLEON, dutch, farFromFortOrange)
        u1 = UnitFactory.create(UnitType.VETERAN_SOLDIER, UnitRole.DRAGOON, dutch, galleon)
        u2 = UnitFactory.create(UnitType.VETERAN_SOLDIER, UnitRole.DRAGOON, dutch, galleon)

        val defence1 = DefenceMission(fortOranje, u1)
        val defence2 = DefenceMission(fortOranje, u2)
        missionContainer.addMission(defence1)
        missionContainer.addMission(defence2)

        // when
        newTurnAndExecuteMission(dutch, 1)

        // then
        PlayerMissionsContainerAssert.assertThat(missionContainer)
            .hasMission(TransportUnitRequestMission::class.java, u1)
            .hasMission(TransportUnitRequestMission::class.java, u2)
    }


    @Test
    fun `should change defence destination when can not disembark`() {
        // given
        val missionContainer = game.aiContainer.missionContainer(dutch)
        val farFromFortOrange = game.map.getTile(29, 89)

        // given units
        galleon = UnitFactory.create(UnitType.GALLEON, dutch, farFromFortOrange)
        u1 = UnitFactory.create(UnitType.VETERAN_SOLDIER, UnitRole.DRAGOON, dutch, galleon)
        u2 = UnitFactory.create(UnitType.VETERAN_SOLDIER, UnitRole.DRAGOON, dutch, galleon)

        // given defence mission and transport
        val defence1 = DefenceMission(fortOranje, u1)
        val defence2 = DefenceMission(fortOranje, u2)
        missionContainer.addMission(defence1)
        missionContainer.addMission(defence2)

        val request1 = TransportUnitRequestMission(game.turn, u1, fortOranje.tile)
        val request2 = TransportUnitRequestMission(game.turn, u2, fortOranje.tile)
        missionContainer.addMission(defence1, request1)
        missionContainer.addMission(defence2, request2)

        val transportMission = TransportUnitMission(galleon)
            .addUnitDest(request1)
            .addUnitDest(request2)
        missionContainer.addMission(transportMission)

        // change defence access
        fortOranje.changeOwner(spain)

        // when
//        printMissions(dutch)
        newTurnAndExecuteMission(dutch, 2)
//        printMissions(dutch)
        // then

        // should change defence destination
        assertThat(defence1.colonyId).isEqualTo(fortNassau.id)
        assertThat(defence2.colonyId).isEqualTo(fortNassau.id)
        TileAssert.assertThat(request1.destination).isEquals(fortNassau.tile)
        TileAssert.assertThat(request2.destination).isEquals(fortNassau.tile)

        TileAssert.assertThat(transportMission.unitsDest.get(0).dest).isEquals(fortNassau.tile)
        TileAssert.assertThat(transportMission.unitsDest.get(1).dest).isEquals(fortNassau.tile)

        assertThat(defence1).isNotDone
        assertThat(defence2).isNotDone
        assertThat(request1).isNotDone
        assertThat(request2).isNotDone
        assertThat(transportMission).isNotDone
    }

}