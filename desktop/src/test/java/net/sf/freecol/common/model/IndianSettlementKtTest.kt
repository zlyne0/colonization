package net.sf.freecol.common.model

import net.sf.freecol.common.model.IndianSettlementAssert.assertThat
import net.sf.freecol.common.model.TileAssert.assertThat
import net.sf.freecol.common.model.player.PlayerAssert.assertThat
import net.sf.freecol.common.model.specification.GoodsType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import promitech.colonization.savegame.Savegame1600BaseClass

class IndianSettlementKtTest : Savegame1600BaseClass() {

    lateinit var settlement: IndianSettlement

    @BeforeEach
    override fun setup() {
        super.setup()
        val settlementTile = game.map.getTile(25, 71)
        settlement = settlementTile.settlement.asIndianSettlement()
    }

    @Test
    fun `should increase population`() {
        // given
        settlement.goodsContainer.increaseGoodsQuantity(GoodsType.FOOD, 250)
        val unitsCountBeforeCalculation = settlement.units.size()

        // when
        val indianSettlementProduction = IndianSettlementProduction()
        indianSettlementProduction.init(game.map, settlement)
        indianSettlementProduction.updateSettlementPopulationGrowth(game, settlement)

        // then
        assertThat(settlement).hasUnitsCount(unitsCountBeforeCalculation + 1)
    }

    @Test
    fun `should decrease population when lack of food`() {
        // given
        settlement.goodsContainer.decreaseToZero(GoodsType.FOOD)
        val unitsCountBeforeCalculation = settlement.units.size()

        // when
        val indianSettlementProduction = IndianSettlementProduction()
        indianSettlementProduction.init(game.map, settlement)
        indianSettlementProduction.updateSettlementPopulationGrowth(game, settlement)

        // then
        assertThat(settlement).hasUnitsCount(unitsCountBeforeCalculation - 1)
    }

    @Test
    fun `should destroy settlement when no population and loack of food`() {
        // given
        val settlementTile = settlement.tile
        val settlementOwner = settlement.owner
        settlement.goodsContainer.decreaseToZero(GoodsType.FOOD)
        givenOneUnitInSettlement()

        // when
        val indianSettlementProduction = IndianSettlementProduction()
        indianSettlementProduction.init(game.map, settlement)
        indianSettlementProduction.updateSettlementPopulationGrowth(game, settlement)

        // then
        assertThat(settlement).hasUnitsCount(0)
        assertThat(settlementTile).hasNotSettlement()
        assertThat(settlementOwner).hasNotSettlement(settlement)

    }

    private fun givenOneUnitInSettlement() {
        val first = settlement.units.first()
        for (entity in ArrayList(settlement.units.entities())) {
            settlement.removeUnit(entity)
        }
        settlement.addUnit(first)
    }
}