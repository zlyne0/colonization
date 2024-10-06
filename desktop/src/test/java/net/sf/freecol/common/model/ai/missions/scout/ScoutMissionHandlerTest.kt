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
import net.sf.freecol.common.model.map.path.PathFinder
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
    fun should_find_scout_destination() {
        // given
        val villageLocation = game.map.getTile(25, 71)
        villageLocation.settlement.asIndianSettlement().resetContact()

        val pathFinder = PathFinder()
        val pathFinder2 = PathFinder()
        val scoutMissionPlaner = ScoutMissionPlaner(game, pathFinder, pathFinder2)

        val scoutLocation = game.map.getTile(25, 86)
        val scout = UnitFactory.create(UnitType.SCOUT, UnitRole.SCOUT, dutch, scoutLocation)

        // when
        val scoutDestination = scoutMissionPlaner.findScoutDestination(scout)

        // then

        assertThat(scoutDestination is ScoutDestination.OtherIsland).isTrue
        if (scoutDestination is ScoutDestination.OtherIsland) {
            assertThat(scoutDestination.tile).isEquals(villageLocation)
        }
    }

    @Test
    fun shouldTransportToMainIslandAndScout() {
        // given
        val missionContainer = game.aiContainer.missionContainer(dutch)

        val villageLocation = game.map.getTile(25, 71)
        villageLocation.settlement.asIndianSettlement().resetContact()

        val scoutLocation = game.map.getTile(25, 86)
        val scout = UnitFactory.create(UnitType.SCOUT, UnitRole.SCOUT, dutch, scoutLocation)

        val shipLocation = game.map.getTile(27, 86)
        val ship = UnitFactory.create(UnitType.CARAVEL, dutch, shipLocation)

        val scoutMission = ScoutMission(scout)
        missionContainer.addMission(scoutMission)

        val transportUnitRequestMission = TransportUnitRequestMission(game.turn, scout, villageLocation)
        missionContainer.addMission(scoutMission, transportUnitRequestMission)

        val transportUnitMission = TransportUnitMission(ship)
        transportUnitMission.addUnitDest(transportUnitRequestMission)
        missionContainer.addMission(transportUnitMission)

        // when
        newTurnAndExecuteMission(dutch, 2)

        // then
        assertThat(ship).isAtLocation(nieuwAmsterdam.tile)
        assertThat(scout).isNextToLocation(villageLocation)
        assertThat(villageLocation.settlement).isScouted()
    }
}