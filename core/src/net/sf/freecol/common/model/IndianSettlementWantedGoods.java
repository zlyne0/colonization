package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap;

import net.sf.freecol.common.model.specification.GoodsType;

public class IndianSettlementWantedGoods {

	public static final int MAX_WANTED_GOODS = 3;
	
    /** Do not buy goods when the price is this low. */
    private static final int TRADE_MINIMUM_PRICE = 3;

	private final List<GoodsType> goodsTypeOrder;
	private final ObjectIntMap<GoodsType> prices;
	private final IndianSettlementProduction production = new IndianSettlementProduction();

	private final Comparator<GoodsType> goodsTypePriceComparator = new Comparator<GoodsType>() {
		@Override
		public int compare(GoodsType goodsType1, GoodsType goodsType2) {
			return prices.get(goodsType2, 0) - prices.get(goodsType1, 0);
		}
	};
	
	public IndianSettlementWantedGoods() {
		goodsTypeOrder = new ArrayList<GoodsType>(Specification.instance.goodsTypes.size());
		prices = new ObjectIntMap<GoodsType>(goodsTypeOrder.size());
	}
	
	public void updateWantedGoods(Map map, IndianSettlement settlement) {
		production.init(map, settlement);
		
		goodsTypeOrder.clear();
		prices.clear();
		
		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
			// The natives do not trade military or non-storable goods.
			if (!goodsType.isStorable() || goodsType.isMilitary()) {
				continue;
			}
			int priceToBuy = production.goodsPriceToBuy(settlement, goodsType, ProductionSummary.CARRIER_SLOT_MAX_QUANTITY);
			if (priceToBuy > ProductionSummary.CARRIER_SLOT_MAX_QUANTITY * TRADE_MINIMUM_PRICE) {
				prices.put(goodsType, priceToBuy);
				goodsTypeOrder.add(goodsType);
			}
		}
		
		Collections.sort(goodsTypeOrder, goodsTypePriceComparator);
		
		//printPotentialWantedGoods(settlement);
		
		settlement.wantedGoods.clear();
		for (int i=0; i<goodsTypeOrder.size() && i < MAX_WANTED_GOODS; i++) {
			settlement.wantedGoods.add(goodsTypeOrder.get(i));
		}
	}

	private void printPotentialWantedGoods(IndianSettlement settlement) {
		System.out.println("wantedGoods " + settlement.getName());
		for (GoodsType goodsType : goodsTypeOrder) {
			System.out.println(goodsType.getId() + " " + prices.get(goodsType, 0));
		}
	}

}
