package promitech.colonization.ai

import net.sf.freecol.common.model.ColonyAssert.assertThat
import net.sf.freecol.common.model.specification.BuildingType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.Savegame1600BaseClass

class ColonyProductionPlanerTest : Savegame1600BaseClass() {

    @Test
    fun `should calculate profit and building type`() {
        val colonyBuildingPlaner = ColonyBuildingPlaner()
        val player = dutch

        colonyBuildingPlaner.productionValueBuildingPlan(player, fortMaurits)!!
            .apply {
                assertThat(profit).isEqualTo(36)
                assertThat(buildingType.id).isEqualTo(BuildingType.DOCKS)
            }
        colonyBuildingPlaner.productionValueBuildingPlan(player, fortOranje)!!
            .apply {
                assertThat(profit).isEqualTo(106)
                assertThat(buildingType.id).isEqualTo(BuildingType.WEAVER_SHOP)
            }
        colonyBuildingPlaner.productionValueBuildingPlan(player, nieuwAmsterdam)!!
            .apply {
                assertThat(profit).isEqualTo(173)
                assertThat(buildingType.id).isEqualTo(BuildingType.FUR_TRADING_POST)
            }
        colonyBuildingPlaner.productionValueBuildingPlan(player, fortNassau)!!
            .apply {
                assertThat(profit).isEqualTo(77)
                assertThat(buildingType.id).isEqualTo(BuildingType.WAREHOUSE)
            }
    }

    @Test
    fun `should generete building queue`() {
        // given
        val colonyBuildingPlaner = ColonyBuildingPlaner()
        val player = dutch

        // when
        colonyBuildingPlaner.generateBuildingQueue(player)

        // then
        assertThat(fortMaurits).hasBuildingQueue(BuildingType.DOCKS)
        assertThat(fortOranje).hasBuildingQueue(BuildingType.WEAVER_SHOP)
        assertThat(nieuwAmsterdam).hasBuildingQueue(BuildingType.FUR_TRADING_POST)
        assertThat(fortNassau).hasBuildingQueue(BuildingType.WAREHOUSE)
    }
}