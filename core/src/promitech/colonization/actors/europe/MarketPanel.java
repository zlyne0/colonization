package promitech.colonization.actors.europe;

import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.player.MarketData;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;

class MarketPanel extends Table {
	
	private java.util.Map<String, MarketGoodsActor> goodActorByType = new HashMap<String, MarketGoodsActor>();	
	
	MarketPanel() {
		defaults().space(10, 10, 10, 10);
	}
	
	public void init(Player player) {
		
        for (GoodsType goodsType : Specification.instance.goodsTypes.sortedEntities()) {
        	if (!goodsType.isStorable()) {
        		continue;
        	}
        	
        	MarketGoodsActor marketGoodsActor = goodActorByType.get(goodsType.getId());
        	if (marketGoodsActor == null) {
        		marketGoodsActor = new MarketGoodsActor(goodsType);
        		goodActorByType.put(goodsType.getId(), marketGoodsActor);
        		add(marketGoodsActor);
        	}
        	MarketData marketData = player.market().marketGoods.getById(goodsType.getId());
        	marketGoodsActor.initPrice(marketData.getSalePrice(), marketData.getBuyPrice());
        }
	}
}
