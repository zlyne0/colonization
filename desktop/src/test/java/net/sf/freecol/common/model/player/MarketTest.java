package net.sf.freecol.common.model.player;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.SaveGameParser;

public class MarketTest {

    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }

	@Test
	public void shouldChangeGoodsPriceWhenBuy() throws Exception {
		// given
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
        Game game = saveGameParser.parse();
        
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
}
