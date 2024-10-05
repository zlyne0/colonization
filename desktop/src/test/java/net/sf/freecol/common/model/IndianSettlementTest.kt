package net.sf.freecol.common.model

import net.sf.freecol.common.model.IndianSettlementAssert.assertThat
import net.sf.freecol.common.model.ProductionSummaryAssert.assertThat
import net.sf.freecol.common.model.TileAssert.assertThat
import net.sf.freecol.common.model.player.PlayerAssert.assertThat
import net.sf.freecol.common.model.specification.AbstractGoods
import net.sf.freecol.common.model.specification.GoodsType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import promitech.colonization.orders.diplomacy.TradeSession
import promitech.colonization.savegame.Savegame1600BaseClass

class IndianSettlementTest : Savegame1600BaseClass() {

    lateinit var settlement: IndianSettlement
    lateinit var tradeGoods: GoodsType
    lateinit var furs: GoodsType
    lateinit var nieuwAmsterdamTile: Tile

    @BeforeEach
    override fun setup() {
        super.setup()
        val settlementTile = game.map.getTile(25, 71)
        settlement = settlementTile.settlement.asIndianSettlement()

        nieuwAmsterdamTile = nieuwAmsterdam.tile

        tradeGoods = goodsType(GoodsType.TRADE_GOODS)
        furs = goodsType(GoodsType.FURS)
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

    @Test
    fun `should calculate goods production`() {
        // given
        val tile = game.map.getSafeTile(19, 78)
        val settlement = tile.getSettlement().asIndianSettlement()

        val isp = IndianSettlementProduction()

        // when
        isp.init(game.map, settlement)
        isp.updateSettlementGoodsProduction(settlement)

        // then
        //System.out.println("is.getGoodsContainer() " + is.getGoodsContainer().cloneGoods());
        assertThat(settlement.getGoodsContainer().cloneGoods())
            .has("model.goods.ore", 1)
            .has("model.goods.cotton", 200)
            .has("model.goods.furs", 200)
            .has("model.goods.tradeGoods", 16)
            .has("model.goods.food", 200)
            .has("model.goods.tobacco", 200)
            .has("model.goods.lumber", 76)
    }

    @Test
    fun canGenerateWantedGoods() {
        // given
        val tile = game.map.getSafeTile(19, 78)
        val settlement = tile.getSettlement().asIndianSettlement()
        settlement.getGoodsContainer().decreaseAllToZero()

        // when
        IndianSettlementWantedGoods.updateWantedGoods(game.map, settlement)

        // then
        Assertions.assertThat(settlement.getWantedGoods()).hasSize(3)
            .extracting<String, RuntimeException> { obj: GoodsType -> obj.getId() }
            .containsAnyOf(
                "model.goods.rum",
                "model.goods.cigars",
                "model.goods.cloth",
                "model.goods.coats"
            )
    }

    @Test
    fun canGenerateGoodsToSellForWagonTrain() {
        // given
        val tile = game.map.getSafeTile(19, 78)
        val `is` = tile.getSettlement().asIndianSettlement()
        val wagonTrain = UnitFactory.create(UnitType.WAGON_TRAIN, dutch, nieuwAmsterdamTile)
        wagonTrain.goodsContainer.increaseGoodsQuantity(tradeGoods, 100)
        val isp = IndianSettlementProduction()
        isp.init(game.map, `is`)

        // when
        val goodsToSell = isp.goodsToSell(`is`, wagonTrain)
//        for (ag in goodsToSell) {
//            println("goodsToSell $ag")
//        }

        // then
        Assertions.assertThat(goodsToSell)
            .hasSize(3)
            .extracting<Int, java.lang.RuntimeException> { obj: AbstractGoods -> obj.quantity }
            .contains(100, 100, 100)
    }

    @Test
    fun canGenerateGoodsToSellForGalleon() {
        // given
        val tile = game.map.getSafeTile(19, 78)
        val settlement = tile.getSettlement().asIndianSettlement()
        val galleon = UnitFactory.create(UnitType.GALLEON, dutch, nieuwAmsterdamTile)
        galleon.goodsContainer.increaseGoodsQuantity(tradeGoods, 100)
        val isp = IndianSettlementProduction()
        isp.init(game.map, settlement)

        // when
        val goodsToSell = isp.goodsToSell(settlement, galleon)
//        for (ag in goodsToSell) {
//            println("goodsToSell $ag")
//        }

        // then
        Assertions.assertThat(goodsToSell)
            .hasSize(3)
            .extracting<Int, java.lang.RuntimeException> { obj: AbstractGoods -> obj.quantity }
            .contains(25, 25, 25)
    }

    @Test
    fun priceForSellGoodsToSettlementWithMissionary() {
        // given
        val galleon = UnitFactory.create(UnitType.GALLEON, dutch, nieuwAmsterdamTile)
        galleon.goodsContainer.increaseGoodsQuantity(tradeGoods, 100)
        val wagonTrain = UnitFactory.create(UnitType.WAGON_TRAIN, dutch, nieuwAmsterdamTile)
        wagonTrain.goodsContainer.increaseGoodsQuantity(tradeGoods, 100)
        val tile = game.map.getSafeTile(19, 78)
        val settlement = tile.getSettlement().asIndianSettlement()
        settlement.changeMissionary(
            UnitFactory.create("model.unit.jesuitMissionary", "model.role.missionary", dutch, settlement.tile),
            game.turn
        )
        val galleonTradeSession = TradeSession(game.map, settlement, galleon)
            .updateSettlementProduction()
        val wagonTrainTradeSession = TradeSession(game.map, settlement, wagonTrain)
            .updateSettlementProduction()

        // when
        val galleonSellOfferPrice = galleonTradeSession.sellOffer(tradeGoods, 100)
        val wagonTrainSellOfferPrice = wagonTrainTradeSession.sellOffer(tradeGoods, 100)

        // then
        Assertions.assertThat(galleonSellOfferPrice).isEqualTo(1270)
        Assertions.assertThat(wagonTrainSellOfferPrice).isEqualTo(1815)
    }

    @Test
    fun priceForSellGoodsToSettlementWithoutMissionary() {
        // given
        val galleon = UnitFactory.create(UnitType.GALLEON, dutch, nieuwAmsterdamTile)
        galleon.goodsContainer.increaseGoodsQuantity(tradeGoods, 100)
        val wagonTrain = UnitFactory.create(UnitType.WAGON_TRAIN, dutch, nieuwAmsterdamTile)
        wagonTrain.goodsContainer.increaseGoodsQuantity(tradeGoods, 100)
        val tile = game.map.getSafeTile(19, 78)
        val settlement = tile.getSettlement().asIndianSettlement()
        if (settlement.hasMissionary()) {
            settlement.removeMissionary()
        }
        val galleonTradeSession = TradeSession(game.map, settlement, galleon)
            .updateSettlementProduction()
        val wagonTrainTradeSession = TradeSession(game.map, settlement, wagonTrain)
            .updateSettlementProduction()

        // when
        val galleonSellOfferPrice = galleonTradeSession.sellOffer(tradeGoods, 100)
        val wagonTrainSellOfferPrice = wagonTrainTradeSession.sellOffer(tradeGoods, 100)

        // then
        Assertions.assertThat(galleonSellOfferPrice).isEqualTo(1050)
        Assertions.assertThat(wagonTrainSellOfferPrice).isEqualTo(1500)
    }

    @Test
    fun canCalculateBuyPriceFromSettlement() {
        // given
        val tile = game.map.getSafeTile(19, 78)
        val settlement = tile.getSettlement().asIndianSettlement()
        settlement.changeMissionary(
            UnitFactory.create("model.unit.jesuitMissionary", "model.role.missionary", dutch, settlement.tile),
            game.turn
        )
        val galleon = UnitFactory.create(UnitType.GALLEON, dutch, nieuwAmsterdamTile)
        galleon.goodsContainer.increaseGoodsQuantity(tradeGoods, 100)
        val wagonTrain = UnitFactory.create(UnitType.WAGON_TRAIN, dutch, nieuwAmsterdamTile)
        wagonTrain.goodsContainer.increaseGoodsQuantity(tradeGoods, 100)
        val galleonTradeSession = TradeSession(game.map, settlement, galleon)
            .updateSettlementProduction()
        val wagonTrainTradeSession = TradeSession(game.map, settlement, wagonTrain)
            .updateSettlementProduction()
        // when
        val galleonBuyPrice = galleonTradeSession.buyOfferPrice(furs, 100)
        val wagonTrainBuyPrice = wagonTrainTradeSession.buyOfferPrice(furs, 100)

        // then
//        println("galleonBuyPrice $galleonBuyPrice")
//        println("wagonTrainBuyPrice $wagonTrainBuyPrice")
        Assertions.assertThat(galleonBuyPrice).isEqualTo(116)
        Assertions.assertThat(wagonTrainBuyPrice).isEqualTo(79)
    }


    private fun givenOneUnitInSettlement() {
        val first = settlement.units.first()
        for (entity in ArrayList(settlement.units.entities())) {
            settlement.removeUnit(entity)
        }
        settlement.addUnit(first)
    }
}