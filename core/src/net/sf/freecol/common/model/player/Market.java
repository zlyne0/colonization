package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class Market extends ObjectWithId {
	
	public final MapIdEntities<MarketData> marketGoods = new MapIdEntities<MarketData>();
	
	// Propagate 5-30% of the original change.
	private static final int PROPAGATED_GOODS_LOWER_BOUND = 5; 
	private static final int PROPAGATED_GOODS_UPPER_BOUND = 30;
	
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

	public boolean hasArrears(GoodsType type) {
        MarketData data = marketGoods.getByIdOrNull(type.getId());
        if (data == null) {
        	return false;
        }
		return data.hasArrears();
	}

    private static TransactionEffectOnMarket TRANSACTION_EFFECT_ON_MARKET = new TransactionEffectOnMarket();
    
	public TransactionEffectOnMarket buyGoods(Game game, Player player, GoodsType goodsType, int goodsAmount) {
		MarketData marketData = requireMarketData(goodsType);
		if (marketData.hasArrears()) {
			throw new IllegalStateException("can not buy goods: " + goodsType + " because of arrears");
		}
		TRANSACTION_EFFECT_ON_MARKET.reset();
		TRANSACTION_EFFECT_ON_MARKET.goodsTypeId = goodsType.getId();
		TRANSACTION_EFFECT_ON_MARKET.quantity = goodsAmount;
       
        TRANSACTION_EFFECT_ON_MARKET.grossPrice = getBidPrice(goodsType, goodsAmount);
		
		if (player.hasNotGold(TRANSACTION_EFFECT_ON_MARKET.grossPrice)) {
			throw new IllegalStateException("Insufficient funds to pay for build");
		}
		player.subtractGold(TRANSACTION_EFFECT_ON_MARKET.grossPrice);
		
		int playerModifiedMarketAmount = goodsAmount;
		if (goodsType.isStorable()) {
			playerModifiedMarketAmount = (int)player.nationType().applyModifier(Modifier.TRADE_BONUS, (float)goodsAmount);
			TRANSACTION_EFFECT_ON_MARKET.setPricesBeforeTransaction(marketData);
			marketData.modifyOnBuyGoods(goodsAmount, TRANSACTION_EFFECT_ON_MARKET.grossPrice, playerModifiedMarketAmount);
			TRANSACTION_EFFECT_ON_MARKET.setPricesAfterTransaction(marketData);
		}
		
		// bought goods amount on market. Can be modified by player modifiers and differ from argument goodsAmount
		propagateTransactionToEuropeanMarkets(game, player, goodsType, -playerModifiedMarketAmount);
		return TRANSACTION_EFFECT_ON_MARKET;
	}

	public TransactionEffectOnMarket sellGoods(Game game, Player player, GoodsType goodsType, int goodsAmount) {
        MarketData marketData = requireMarketData(goodsType);
		if (marketData.hasArrears()) {
			throw new IllegalStateException("can not sell goods: " + goodsType + " because of arrears");
		}
		
		TRANSACTION_EFFECT_ON_MARKET.reset();
		
		TRANSACTION_EFFECT_ON_MARKET.sell(goodsType, goodsAmount, marketData, player.getTax());
		player.addGold(TRANSACTION_EFFECT_ON_MARKET.netPrice);
        
        int playerModifiedMarketAmount = (int)player.nationType().applyModifier(Modifier.TRADE_BONUS, (float)goodsAmount);
		TRANSACTION_EFFECT_ON_MARKET.setPricesBeforeTransaction(marketData);
        marketData.modifyOnSellGoods(goodsAmount, TRANSACTION_EFFECT_ON_MARKET.grossPrice, TRANSACTION_EFFECT_ON_MARKET.netPrice, playerModifiedMarketAmount);
        TRANSACTION_EFFECT_ON_MARKET.setPricesAfterTransaction(marketData);
        
        propagateTransactionToEuropeanMarkets(game, player, goodsType, playerModifiedMarketAmount);
        return TRANSACTION_EFFECT_ON_MARKET;
	}
	
	private void propagateTransactionToEuropeanMarkets(Game game, Player owner, GoodsType goodsType, int goodsAmount) {
		if (!goodsType.isStorable()) {
			return;
		}
		goodsAmount = modifyGoodsAmountPropagatetToMarkets(goodsAmount);
        if (goodsAmount == 0) {
        	return;
        }
        for (Player player : game.players.entities()) {
        	if (player.isNotLiveEuropeanPlayer()) {
        		continue;
        	}
        	if (player.equalsId(owner)) {
        		continue;
        	}
        	player.market().addGoodsToMarket(goodsType, goodsAmount);
        }
	}

	private final int modifyGoodsAmountPropagatetToMarkets(int amount) {
        amount *= Randomizer.getInstance().randomInt(PROPAGATED_GOODS_LOWER_BOUND, PROPAGATED_GOODS_UPPER_BOUND);
        amount /= 100;
		return amount;
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
	
	public boolean canTradeInEurope(String goodsTypeId) {
		MarketData marketData = marketGoods.getByIdOrNull(goodsTypeId);
		if (marketData == null || marketData.hasNotArrears()) {
			return true;
		}
		return false;
	}
	
	public boolean canTradeInCustomHouse(Game game, Player player, String goodsTypeId) {
		MarketData marketData = marketGoods.getByIdOrNull(goodsTypeId);
		if (marketData == null || marketData.hasNotArrears()) {
			return true;
		}
        if (Specification.options.getBoolean(GameOptions.CUSTOM_IGNORE_BOYCOTT)) {
            return true;
        }
        if (player.getFeatures().hasAbility(Ability.CUSTOM_HOUSE_TRADES_WITH_FOREIGN_COUNTRIES)) {
        	if (player.hasPeaceOrAllianceWithOneOfEuropeanPlayers(game)) {
        		return true;
        	}
        }
		return false;
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
