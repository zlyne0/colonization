package promitech.colonization.ai

import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.colonyproduction.ColonyPlan
import net.sf.freecol.common.model.specification.BuildingType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.Savegame1600BaseClass
import java.util.function.Function

class ColonyProductionPlanerTest : Savegame1600BaseClass() {

    @Test
    fun `should generete building recomendation and calculate profit`() {
        // given
        val player = dutch
        val colonyProductionPlaner = ColonyProductionPlaner(player)

        // when
        val settlementsBuildingValue = colonyProductionPlaner.buildRecommendations()

        // then
        assertThat(settlementsBuildingValue).hasSize(4)
        with(settlementsBuildingValue.get(0)) {
            assertThat(colony.equalsId(nieuwAmsterdam))
            assertThat(buildingTypeScore!!.buildingType.equalsId(BuildingType.FUR_TRADING_POST))
            assertThat(buildingTypeScore!!.profit).isEqualTo(108)
        }
        with(settlementsBuildingValue.get(1)) {
            assertThat(colony.equalsId(fortOranje))
            assertThat(buildingTypeScore!!.buildingType.equalsId(BuildingType.WEAVER_SHOP))
            assertThat(buildingTypeScore!!.profit).isEqualTo(102)
        }
        with(settlementsBuildingValue.get(2)) {
            assertThat(colony.equalsId(fortNassau))
            assertThat(buildingTypeScore!!.buildingType.equalsId(BuildingType.FUR_TRADING_POST))
            assertThat(buildingTypeScore!!.profit).isEqualTo(66)
        }
        with(settlementsBuildingValue.get(3)) {
            assertThat(colony.equalsId(fortMaurits))
            assertThat(buildingTypeScore).isNull()
        }
    }

    @Test
    fun `should generate colony plan profile`() {
        // given
        val player = dutch
        val colonyProductionPlaner = ColonyProductionPlaner(player)

        // when
        val coloniesProductionProfile = colonyProductionPlaner.generateColonyPlanProductionRecommendations()

        //colonyProductionPlaner.prettyPrintSettlementsBuildingValue(colonyProductionPlaner.calculateSettlementsBuildingValue(player))

        // then
        assertThat(coloniesProductionProfile).hasSize(4)
        with(coloniesProductionProfile.get(0)) {
            assertThat(colony.equalsId(nieuwAmsterdam))
            assertThat(productionProfile).isEqualTo(ColonyPlan.ProductionProfile.MostValuable)
        }
        with(coloniesProductionProfile.get(1)) {
            assertThat(colony.equalsId(fortOranje))
            assertThat(productionProfile).isEqualTo(ColonyPlan.ProductionProfile.Building)
        }
        with(coloniesProductionProfile.get(2)) {
            assertThat(colony.equalsId(fortNassau))
            assertThat(productionProfile).isEqualTo(ColonyPlan.ProductionProfile.MostValuable)
        }
        with(coloniesProductionProfile.get(3)) {
            assertThat(colony.equalsId(fortMaurits))
            assertThat(productionProfile).isEqualTo(ColonyPlan.ProductionProfile.MostValuable)
        }
    }

    @Test
    fun `should sort settlements building plan`() {
        // given
        val warehouse = Specification.instance.buildingTypes.getById(BuildingType.WAREHOUSE)

        val list = listOf(
            BuildRecommendation(nieuwAmsterdam, 101, null),
            BuildRecommendation(fortNassau, 101, BuildingTypeScore(warehouse, 50, fortNassau, 1500, TurnRateOfReturn(10))),
            BuildRecommendation(fortOranje, 100, BuildingTypeScore(warehouse, 54, fortOranje, 1500, TurnRateOfReturn(5))),
            BuildRecommendation(fortMaurits, 100, BuildingTypeScore(warehouse, 54, fortMaurits, 1500, TurnRateOfReturn(4)))
        )

        // when
        val sortedList = list.sortedWith(BuildRecommendation.firstHigherBuildingTypeProfit)

        // then
        assertThat(sortedList)
            .extracting(Function { pps -> pps.colony.id })
            .containsExactly(
                fortMaurits.id,
                fortOranje.id,
                fortNassau.id,
                nieuwAmsterdam.id
            )
    }

    @Test
    fun `should return max profit for two BuildingTypeRecommendation`() {
        // given
        val warehouse = Specification.instance.buildingTypes.getById(BuildingType.WAREHOUSE)

        val b1 = BuildingTypeScore(warehouse, 54, fortOranje, 1500, TurnRateOfReturn(5))
        val b2 = BuildingTypeScore(warehouse, 54, fortMaurits, 1500, TurnRateOfReturn(4))

        // when
        val max = b2.maxProfit(b1)

        // then
        assertThat(max.colony.id).isEqualTo(fortMaurits.id)
    }
}