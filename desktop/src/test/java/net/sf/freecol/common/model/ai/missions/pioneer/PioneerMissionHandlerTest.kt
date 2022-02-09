package net.sf.freecol.common.model.ai.missions.pioneer

import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.map.path.PathFinder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PioneerMissionHandlerTest : MissionHandlerBaseTestClass() {

    @BeforeEach
    override fun setup() {
        super.setup()

        clearAllMissions(dutch)
    }




}