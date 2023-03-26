package promitech.colonization.ai

import net.sf.freecol.common.model.ColonyAssert.assertThat
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.ai.missions.MissionHandlerBaseTestClass
import net.sf.freecol.common.model.specification.BuildingType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EuropeanMissionPlanerTest : MissionHandlerBaseTestClass() {

    @BeforeEach
    override fun setup() {
        super.setup()

        clearAllMissions(dutch)
    }

    @Test
    fun `should buy building`() {
        // given
        val player = dutch
        val missionContainer = game.aiContainer.missionContainer(dutch)
        val planer = EuropeanMissionPlaner(game, di.pathFinder, di.pathFinder2)

        val weaverShop = Specification.instance.buildingTypes.getById(BuildingType.WEAVER_SHOP)
        player.addGold(10000)

        // and
        assertThat(fortOranje).hasNotBuilding(weaverShop.id)

        // when
        planer.prepareMissions(player, missionContainer)

        // then
        assertThat(fortOranje)
            .hasCompletedBuilding(weaverShop)
            .hasBuildingQueue(weaverShop.id)

        // when
        di.newTurnService.newTurn(player)

        // then
        assertThat(fortOranje).hasBuilding(weaverShop.id)
    }
}