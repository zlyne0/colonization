package net.sf.freecol.common.model.ai.missions.scout

import net.sf.freecol.common.model.IndianSettlementAssert
import net.sf.freecol.common.model.IndianSettlementAssert.*
import net.sf.freecol.common.model.UnitAssert
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ScoutMissionHandlerTest : MissionHandlerBaseTestClass() {

    @BeforeEach
    override fun setup() {
        super.setup()

        clearAllMissions(dutch)
    }

    @Test
    fun shouldTransportToMainIslandAndScout() {
        // given
        val villageLocation = game.map.getTile(25, 71)
        assertThat(villageLocation.settlement).isNotScouted()

        val scoutLocation = game.map.getTile(25, 86)
        val scout = UnitFactory.create(UnitType.SCOUT, UnitRole.SCOUT, dutch, scoutLocation)

        val shipLocation = game.map.getTile(27, 86)
        val ship = UnitFactory.create(UnitType.CARAVEL, dutch, shipLocation)

        val scoutMission = ScoutMission(scout)
        val missionContainer = game.aiContainer.missionContainer(dutch)
        missionContainer.addMission(scoutMission)

        // when
        newTurnAndExecuteMission(dutch, 1)
        // when
        assertThat(scoutMission.phase).isEqualTo(ScoutMission.Phase.WAIT_FOR_TRANSPORT)
        assertThat(scoutMission.scoutDistantDestination).isNotNull()

        // when
        val transportUnitMission = TransportUnitMission(ship)
        transportUnitMission.addUnitDest(scoutMission.scout, scoutMission.scoutDistantDestination, true)
        scoutMission.addDependMission(transportUnitMission)
        newTurnAndExecuteMission(dutch, 3)
        // then
        UnitAssert.assertThat(ship).isAtLocation(fortOranje.tile)
        UnitAssert.assertThat(scout).isAtLocation(fortOranje.tile)

        // when
        newTurnAndExecuteMission(dutch, 1)
        // then
        UnitAssert.assertThat(scout).isNextToLocation(villageLocation)
        assertThat(villageLocation.settlement).isScouted()
    }
}