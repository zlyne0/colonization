package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Market extends ObjectWithId {
	public final MapIdEntities<MarketData> marketGoods = new MapIdEntities<MarketData>();
	
	public Market(String id) {
		super(id);
	}

	public int buildingGoodsPrice(GoodsType goodsType, int amount) {
		if (goodsType.isStorable()) {
			return (getBidPrice(goodsType, amount) * 110) / 100;
		} else {
			return goodsType.getPrice() * amount;
		}
	}
	
    public int getBidPrice(GoodsType type, int amount) {
        MarketData data = marketGoods.getByIdOrNull(type.getId());
        return (data == null) ? 0 : amount * data.getCostToBuy();
    }
	
	public static class Xml extends XmlNodeParser {
		public Xml() {
			addNodeForMapIdEntities("marketGoods", MarketData.class);
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute("id");
			Market market = new Market(id);
			nodeObject = market;
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "market";
		}
		
	}

}
