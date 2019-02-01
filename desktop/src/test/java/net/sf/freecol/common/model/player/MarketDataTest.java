package net.sf.freecol.common.model.player;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.SaveGameParser;

public class MarketDataTest {

    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }
    
    @Test
    public void canInitMarketData() throws Exception {
        // given
        SaveGameParser.loadDefaultSpecification();
        GoodsType silver = Specification.instance.goodsTypes.getById("model.goods.silver");

        // when
        MarketData marketData = new MarketData(silver);
        marketData.update();
        
        // then
        assertThat(marketData.getSalePrice()).isEqualTo(16);
        assertThat(marketData.getBuyPrice()).isEqualTo(18);
    }
    
    
}
