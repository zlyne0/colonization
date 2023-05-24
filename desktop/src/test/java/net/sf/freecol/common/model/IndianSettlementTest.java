package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import promitech.colonization.orders.diplomacy.TradeSession;
import promitech.colonization.savegame.Savegame1600BaseClass;

import static org.assertj.core.api.Assertions.assertThat;

class IndianSettlementTest extends Savegame1600BaseClass {

	GoodsType tradeGoods;
	GoodsType furs;
	Tile nieuwAmsterdamTile;

    @BeforeEach
    public void setup() throws Exception {
		super.setup();
        tradeGoods = goodsType(GoodsType.TRADE_GOODS);
        furs = goodsType(GoodsType.FURS);
        
		nieuwAmsterdamTile = nieuwAmsterdam.tile;
    }
	
	@Test
	public void canGenerateWantedGoods() {
		// given
		Tile tile = game.map.getSafeTile(19, 78);
		
		IndianSettlement is = tile.getSettlement().asIndianSettlement();
		is.getGoodsContainer().decreaseAllToZero();
		
		IndianSettlementWantedGoods sut = new IndianSettlementWantedGoods();
		
		// when
		sut.updateWantedGoods(game.map, is);
		
		// then
		assertThat(is.getWantedGoods()).hasSize(3)
			.extracting(GoodsType::getId)
			.containsAnyOf(
				"model.goods.rum", 
				"model.goods.cigars", 
				"model.goods.cloth", 
				"model.goods.coats" 		
			);
	}
	
	@Test
	public void canGenerateGoodsToSellForWagonTrain() throws Exception {
		// given
		Tile tile = game.map.getSafeTile(19, 78);
		IndianSettlement is = tile.getSettlement().asIndianSettlement();

		Unit wagonTrain = UnitFactory.create(UnitType.WAGON_TRAIN, dutch, nieuwAmsterdamTile);
		wagonTrain.getGoodsContainer().increaseGoodsQuantity(tradeGoods, 100);
		
		IndianSettlementProduction isp = new IndianSettlementProduction();
		isp.init(game.map, is);
		
		// when
		List<? extends AbstractGoods> goodsToSell = isp.goodsToSell(is, wagonTrain);
		
		for (AbstractGoods ag : goodsToSell) {
			System.out.println("goodsToSell " + ag);
		}
		
		// then
		assertThat(goodsToSell)
			.hasSize(3)
			.extracting(AbstractGoods::getQuantity)
			.contains(100, 100, 100);
	}

	@Test
	public void canGenerateGoodsToSellForGalleon() throws Exception {
		// given
		Tile tile = game.map.getSafeTile(19, 78);
		IndianSettlement is = tile.getSettlement().asIndianSettlement();

		Unit galleon = UnitFactory.create(UnitType.GALLEON, dutch, nieuwAmsterdamTile);
		galleon.getGoodsContainer().increaseGoodsQuantity(tradeGoods, 100);
		
		IndianSettlementProduction isp = new IndianSettlementProduction();
		isp.init(game.map, is);
		
		// when
		List<? extends AbstractGoods> goodsToSell = isp.goodsToSell(is, galleon);
		
		for (AbstractGoods ag : goodsToSell) {
			System.out.println("goodsToSell " + ag);
		}
		
		// then
		assertThat(goodsToSell)
			.hasSize(3)
			.extracting(AbstractGoods::getQuantity)
			.contains(25, 25, 25);
	}
	
	@Test
	public void priceForSellGoodsToSettlementWithMissionary() throws Exception {
		// given
		Unit galleon = UnitFactory.create(UnitType.GALLEON, dutch, nieuwAmsterdamTile);
		galleon.getGoodsContainer().increaseGoodsQuantity(tradeGoods, 100);
		Unit wagonTrain = UnitFactory.create(UnitType.WAGON_TRAIN, dutch, nieuwAmsterdamTile);
		wagonTrain.getGoodsContainer().increaseGoodsQuantity(tradeGoods, 100);

		Tile tile = game.map.getSafeTile(19, 78);
		IndianSettlement is = tile.getSettlement().asIndianSettlement();
		is.changeMissionary(UnitFactory.create("model.unit.jesuitMissionary", "model.role.missionary", dutch, is.tile));
		
		TradeSession galleonTradeSession = new TradeSession(game.map, is, galleon)
			.updateSettlementProduction();
		TradeSession wagonTrainTradeSession = new TradeSession(game.map, is, wagonTrain)
			.updateSettlementProduction();
		
		// when
		int galleonSellOfferPrice = galleonTradeSession.sellOffer(tradeGoods, 100);
		int wagonTrainSellOfferPrice = wagonTrainTradeSession.sellOffer(tradeGoods, 100);

		// then
		assertThat(galleonSellOfferPrice).isEqualTo(1270);
		assertThat(wagonTrainSellOfferPrice).isEqualTo(1815);
	}

	@Test
	public void priceForSellGoodsToSettlementWithoutMissionary() throws Exception {
		// given
		Unit galleon = UnitFactory.create(UnitType.GALLEON, dutch, nieuwAmsterdamTile);
		galleon.getGoodsContainer().increaseGoodsQuantity(tradeGoods, 100);
		Unit wagonTrain = UnitFactory.create(UnitType.WAGON_TRAIN, dutch, nieuwAmsterdamTile);
		wagonTrain.getGoodsContainer().increaseGoodsQuantity(tradeGoods, 100);

		Tile tile = game.map.getSafeTile(19, 78);
		IndianSettlement is = tile.getSettlement().asIndianSettlement();
		if (is.hasMissionary()) {
			is.removeMissionary();
		}
		
		TradeSession galleonTradeSession = new TradeSession(game.map, is, galleon)
			.updateSettlementProduction();
		TradeSession wagonTrainTradeSession = new TradeSession(game.map, is, wagonTrain)
			.updateSettlementProduction();
		
		// when
		int galleonSellOfferPrice = galleonTradeSession.sellOffer(tradeGoods, 100);
		int wagonTrainSellOfferPrice = wagonTrainTradeSession.sellOffer(tradeGoods, 100);

		// then
		assertThat(galleonSellOfferPrice).isEqualTo(1050);
		assertThat(wagonTrainSellOfferPrice).isEqualTo(1500);
	}

	@Test
	public void canCalculateBuyPriceFromSettlement() throws Exception {
		// given
		Tile tile = game.map.getSafeTile(19, 78);
		IndianSettlement is = tile.getSettlement().asIndianSettlement();
		is.changeMissionary(UnitFactory.create("model.unit.jesuitMissionary", "model.role.missionary", dutch, is.tile));
		
		Unit galleon = UnitFactory.create(UnitType.GALLEON, dutch, nieuwAmsterdamTile);
		galleon.getGoodsContainer().increaseGoodsQuantity(tradeGoods, 100);
		Unit wagonTrain = UnitFactory.create(UnitType.WAGON_TRAIN, dutch, nieuwAmsterdamTile);
		wagonTrain.getGoodsContainer().increaseGoodsQuantity(tradeGoods, 100);
		
		TradeSession galleonTradeSession = new TradeSession(game.map, is, galleon)
			.updateSettlementProduction();
		TradeSession wagonTrainTradeSession = new TradeSession(game.map, is, wagonTrain)
			.updateSettlementProduction();
		// when
		int galleonBuyPrice = galleonTradeSession.buyOfferPrice(furs, 100);
		int wagonTrainBuyPrice = wagonTrainTradeSession.buyOfferPrice(furs, 100);

		// then
		System.out.println("galleonBuyPrice " + galleonBuyPrice);
		System.out.println("wagonTrainBuyPrice " + wagonTrainBuyPrice);
		
		assertThat(galleonBuyPrice).isEqualTo(116);
		assertThat(wagonTrainBuyPrice).isEqualTo(79);
	}
	
	@Test
    void newTurnProduction() throws Exception {
        // given
        Tile tile = game.map.getSafeTile(19, 78);
        
        IndianSettlement is = tile.getSettlement().asIndianSettlement();

        IndianSettlementProduction isp = new IndianSettlementProduction();

        isp.init(game.map, is);
        
        // when
        isp.updateSettlementGoodsProduction(is);

        // then
        //System.out.println("is.getGoodsContainer() " + is.getGoodsContainer().cloneGoods());
        ProductionSummaryAssert.assertThat(is.getGoodsContainer().cloneGoods())
            .has("model.goods.ore", 1) 
            .has("model.goods.cotton", 200)
            .has("model.goods.furs", 200)
            .has("model.goods.tradeGoods", 16) 
            .has("model.goods.food", 200)
            .has("model.goods.tobacco", 200)
            .has("model.goods.lumber", 76)
        ;
        
    }
	
	
}
