package net.sf.freecol.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.orders.diplomacy.TradeSession;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.resources.Messages;

class IndianSettlementTest {

	Game game;
	GoodsType tradeGoods;
	Tile nieuwAmsterdamTile;
	Player dutch;
	
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
        Messages.instance().load();
    }
    
    @BeforeEach
    public void setup() throws IOException, ParserConfigurationException, SAXException {
        game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
        tradeGoods = Specification.instance.goodsTypes.getById("model.goods.tradeGoods");
        
		nieuwAmsterdamTile = game.map.getSafeTile(24, 78);
		dutch = game.players.getById("player:1");
    }
	
	@Test
	public void canGenerateWantedGoods() {
		// given
		Tile tile = game.map.getSafeTile(19, 78);
		
		IndianSettlement is = tile.getSettlement().getIndianSettlement();
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
		IndianSettlement is = tile.getSettlement().getIndianSettlement();

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
		IndianSettlement is = tile.getSettlement().getIndianSettlement();

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
		IndianSettlement is = tile.getSettlement().getIndianSettlement();
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
		IndianSettlement is = tile.getSettlement().getIndianSettlement();
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
	
	
}
