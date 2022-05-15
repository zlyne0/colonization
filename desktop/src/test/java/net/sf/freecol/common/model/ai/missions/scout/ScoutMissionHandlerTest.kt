package net.sf.freecol.common.model.ai.missions.scout

import net.sf.freecol.common.model.IndianSettlementAssert.assertThat
import net.sf.freecol.common.model.TileAssert.assertThat
import net.sf.freecol.common.model.UnitAssert.assertThat
import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.ai.missions.TransportUnitMission
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission
import org.assertj.core.api.Assertions.assertThat
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
        val missionContainer = game.aiContainer.missionContainer(dutch)

        val villageLocation = game.map.getTile(25, 71)
        assertThat(villageLocation.settlement).isNotScouted()

        val scoutLocation = game.map.getTile(25, 86)
        val scout = UnitFactory.create(UnitType.SCOUT, UnitRole.SCOUT, dutch, scoutLocation)

        val shipLocation = game.map.getTile(27, 86)
        val ship = UnitFactory.create(UnitType.CARAVEL, dutch, shipLocation)

        val scoutMission = ScoutMission(scout)
        missionContainer.addMission(scoutMission)

        // when
        newTurnAndExecuteMission(dutch, 1)
        // when
        assertThat(scoutMission.phase).isEqualTo(ScoutMission.Phase.WAIT_FOR_TRANSPORT)
        assertThat(scoutMission.scoutDistantDestination)
            .isNotNull()
            .isEquals(villageLocation)

        // when
        val transportRequest = missionContainer.findFirstMission(TransportUnitRequestMission::class.java, scout)

        val transportUnitMission = TransportUnitMission(ship)
        transportUnitMission.addUnitDest(transportRequest)
        missionContainer.addMission(transportUnitMission)
        newTurnAndExecuteMission(dutch, 3)

        // then
        assertThat(ship).isAtLocation(fortOranje.tile)
        assertThat(scout).isNextToLocation(villageLocation)
        assertThat(villageLocation.settlement).isScouted()
    }
}