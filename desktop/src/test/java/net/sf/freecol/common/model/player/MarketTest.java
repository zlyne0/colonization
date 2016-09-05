package net.sf.freecol.common.model.player;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.SaveGameParser;

public class MarketTest {

	SaveGameParser saveGameParser;
	Game game;
	
    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }

    @Before
    public void setup() throws IOException, ParserConfigurationException, SAXException {
    	saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
    	game = saveGameParser.parse();
    }

	@Test
	public void shouldChangeGoodsPriceWhenBuy() throws Exception {
		// given
        int goodsAmount = 100;
        String goodsTypeStr = "model.goods.cigars";
        
        GoodsType goodsType = Specification.instance.goodsTypes.getById(goodsTypeStr);
        Player player = game.players.getById("player:1");
        Market market = player.market();
		
        int beforeBuyPrice = market.getBidPrice(goodsType, 100);
		
        MarketData marketData = market.requireMarketData(goodsType);
        
		// when
        boolean modifyOnBuyGoods = marketData.modifyOnBuyGoods(goodsAmount, beforeBuyPrice, goodsAmount);
        
		// then
		assertTrue(modifyOnBuyGoods);
        assertEquals(1200, beforeBuyPrice);
        int afterBuyPrice = market.getBidPrice(goodsType, 100);
        assertEquals(1300, afterBuyPrice);
        
        System.out.println("before buy price = " + beforeBuyPrice);
        System.out.println("modifyOnBuyGoods = " + modifyOnBuyGoods);
        System.out.println("after buy price = " + afterBuyPrice);
	}
	
	@Test
	public void shouldChangeGoodsPriceWhenSellByOtherPlayer() throws Exception {
		// given
        int goodsAmount = 300;
        String silverTypeStr = "model.goods.silver";
        
        GoodsType silver = Specification.instance.goodsTypes.getById(silverTypeStr);
        Player player1 = game.players.getById("player:1");
        Player player2 = game.players.getById("player:112");

        MarketSnapshoot marketSnapshoot = new MarketSnapshoot(player2.market());
        
		// when
        TransactionEffectOnMarket player1Transaction = player1.market().sellGoods(game, player1, silver, goodsAmount);
        
		// then
        assertTrue(player1Transaction.isMarketPriceChanged());
        assertFalse(player1Transaction.isPriceIncrease());

		MarketChangePrice mcp = marketSnapshoot.prices.getByIdOrNull(silverTypeStr);
		mcp.setPricesAfterTransaction(player2.market().marketGoods.getById(silverTypeStr));
		assertTrue(mcp.isMarketPriceChanged());
	}
	
}
