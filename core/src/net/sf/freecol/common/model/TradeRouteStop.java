package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.specification.GoodsType;

public class TradeRouteStop {
	private String tradeLocationId;
	private List<GoodsType> goodsType = new ArrayList<GoodsType>();

	public TradeRouteStop(String tradeLocationId) {
		this.tradeLocationId = tradeLocationId;
	}
	
	public String getTradeLocationId() {
		return tradeLocationId;
	}

	public List<GoodsType> getGoodsType() {
		return goodsType;
	}
}
