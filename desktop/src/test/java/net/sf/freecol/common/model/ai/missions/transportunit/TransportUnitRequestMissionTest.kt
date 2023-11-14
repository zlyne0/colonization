package net.sf.freecol.common.model.ai.missions.transportunit

import net.sf.freecol.common.model.UnitFactory
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainerAssert
import org.junit.jupiter.api.Test

class TransportUnitRequestMissionTest : MissionHandlerBaseTestClass() {

    @Test
    fun `transport request should end when its on destination`() {
        // given
        val fortOrangeTile = fortOranje.tile

        val missionContainer = game.aiContainer.missionContainer(dutch)
        val unit = UnitFactory.create(UnitType.FREE_COLONIST, dutch, fortOrangeTile)
        val request1 = TransportUnitRequestMission(game.turn, unit, fortOrangeTile)
        missionContainer.addMission(request1)

        // when
        newTurnAndExecuteMission(dutch, 1)

        // then
        PlayerMissionsContainerAssert.assertThat(missionContainer)
            .doesNotHaveMission(request1).isDone(request1)
    }


}