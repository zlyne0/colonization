package promitech.colonization.ai.military

import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.specification.BuildingType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import promitech.colonization.ai.military.DefencePrice.DefencePurchase.ColonyBuildingPurchase
import promitech.colonization.ai.military.DefencePrice.DefencePurchase.UnitPurchase
import promitech.colonization.savegame.Savegame1600BaseClass

class DefencePriceTest : Savegame1600BaseClass() {

    @Test
    fun `should generate request`() {
        // given

        // when
        val defencePurchases = DefencePrice().generateRequests(3f, nieuwAmsterdam)

        // then
        for (defencePurchase in defencePurchases) {
            println(defencePurchase)
        }
        with((defencePurchases[0] as ColonyBuildingPurchase)) {
            assertThat(buildingType.id).isEqualTo(BuildingType.STOCKADE)
            assertThat(price).isEqualTo(1280)
            assertThat(tileDefenceAfterPurchase).isEqualTo(6f)
        }
        with((defencePurchases[1] as UnitPurchase)) {
            assertThat(unitType.id).isEqualTo(UnitType.ARTILLERY)
            assertThat(price).isEqualTo(500)
            assertThat(tileDefenceAfterPurchase).isEqualTo(8f)
        }
    }


}