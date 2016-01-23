package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.GoodsContainer;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Modifier;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Market extends ObjectWithId {
	public final MapIdEntities<MarketData> marketGoods = new MapIdEntities<MarketData>();
	
	// Propagate 5-30% of the original change.
	private static final int PROPAGATED_GOODS_LOWER_BOUND = 5; 
	private static final int PROPAGATED_GOODS_UPPER_BOUND = 30;
	
	public static final int modifyGoodsAmountPropagatetToMarkets(int amount) {
        amount *= Randomizer.getInstance().randomInt(PROPAGATED_GOODS_LOWER_BOUND, PROPAGATED_GOODS_UPPER_BOUND);
        amount /= 100;
		return amount;
	}
	
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
        return (data == null) ? 0 : data.getCostToBuy(amount);
    }

    private static TransactionEffectOnMarket TRANSACTION_EFFECT_ON_MARKET = new TransactionEffectOnMarket();
    
	public TransactionEffectOnMarket buyGoods(Player player, GoodsType goodsType, int goodsAmount, GoodsContainer goodsContainer) {
		int price = getBidPrice(goodsType, goodsAmount);
		if (player.hasNotGold(price)) {
			throw new IllegalStateException("Insufficient funds to pay for build");
		}
		player.subtractGold(price);
		goodsContainer.increaseGoodsQuantity(goodsType, goodsAmount);
		
		int playerModifiedMarketAmount = goodsAmount;
		if (goodsType.isStorable()) {
			playerModifiedMarketAmount = (int)player.nationType().applyModifier(Modifier.TRADE_BONUS, (float)goodsAmount);
			
			MarketData marketData = requireMarketData(goodsType);
			TRANSACTION_EFFECT_ON_MARKET.beforePrice = marketData.getCostToBuy(goodsAmount);
			marketData.modifyOnBuyGoods(goodsAmount, price, playerModifiedMarketAmount);
			TRANSACTION_EFFECT_ON_MARKET.afterPrice = marketData.getCostToBuy(goodsAmount);
		}
		
		// bought goods amount on market. Can be modified by player modifiers and differ from argument goodsAmount
		TRANSACTION_EFFECT_ON_MARKET.goodsModifiedMarket = playerModifiedMarketAmount;
		return TRANSACTION_EFFECT_ON_MARKET;
	}

    public boolean addGoodsToMarket(GoodsType goodsType, int amount) {
        MarketData data = requireMarketData(goodsType);
        data.modifyAmountInMarket(amount);
        return data.price();
    }
	
    protected MarketData requireMarketData(GoodsType goodsType) {
    	MarketData marketData = marketGoods.getByIdOrNull(goodsType.getId());
    	if (marketData == null) {
    		marketData = new MarketData(goodsType);
    		marketData.update();
    		marketGoods.add(marketData);
    	}
    	return marketData;
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
