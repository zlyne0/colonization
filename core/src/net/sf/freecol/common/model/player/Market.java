package net.sf.freecol.common.model.player;

import java.io.IOException;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class Market extends ObjectWithId {
	
	public static interface MarketTransactionLogger {
		void logSale(TransactionEffectOnMarket transaction);
		void logPurchase(TransactionEffectOnMarket transaction);
	}

	public static final MarketTransactionLogger emptyMarketTransactionLogger = new MarketTransactionLogger() {
		@Override
		public void logSale(TransactionEffectOnMarket transaction) {
		}

		@Override
		public void logPurchase(TransactionEffectOnMarket transaction) {
		}
	};

	public final MapIdEntities<MarketData> marketGoods = MapIdEntities.linkedMapIdEntities();
	private MarketTransactionLogger marketTransactionLogger = emptyMarketTransactionLogger;
	
	// Propagate 5-30% of the original change.
	private static final int PROPAGATED_GOODS_LOWER_BOUND = 5; 
	private static final int PROPAGATED_GOODS_UPPER_BOUND = 30;
	
	public Market(String id) {
		super(id);
	}

	private interface GoodsPriceSpecification {
		int price(GoodsType goodsType, int amount); 
	}
	
	private final GoodsPriceSpecification buildingGoodsPrice = new GoodsPriceSpecification() {
		@Override
		public int price(GoodsType goodsType, int amount) {
			if (goodsType.isStorable()) {
			    // price for buy goods for buildings is 10% higher than in market
				return (getBidPrice(goodsType, amount) * 110) / 100;
			} else {
				return goodsType.getPrice() * amount;
			}
		}
	};
	private final GoodsPriceSpecification marketGoodsPrice = new GoodsPriceSpecification() {
		@Override
		public int price(GoodsType goodsType, int amount) {
			if (goodsType.isStorable()) {
				MarketData data = marketGoods.getByIdOrNull(goodsType.getId());
				if (data == null) {
					throw new IllegalStateException("no goods type[" + goodsType + "] on market");
				}
				return (data == null) ? 0 : data.getCostToBuy(amount);
			}
			throw new IllegalStateException("can not buy not storable goods(" + goodsType.getId() + ") on market");
		}
	};
	
	public int buildingGoodsPrice(GoodsType goodsType, int amount) {
		return buildingGoodsPrice.price(goodsType, amount);
	}
	
    public int getBidPrice(GoodsType goodsType, int amount) {
    	return marketGoodsPrice.price(goodsType, amount);
    }

    public int getSalePrice(GoodsType type, int amount) {
    	MarketData data = marketGoods.getByIdOrNull(type.getId());
    	if (data == null) {
    		return 0;
    	}
    	return data.getCostToSell(amount);
    }
    
	public boolean hasArrears(GoodsType type) {
        MarketData data = marketGoods.getByIdOrNull(type.getId());
        if (data == null) {
        	return false;
        }
		return data.hasArrears();
	}

	public boolean canAffordFor(Player player, ProductionSummary required) {
		int paidSum = 0;
		for (Entry<String> goodsTypeEntry : required.entries()) {
			MarketData data = marketGoods.getByIdOrNull(goodsTypeEntry.key);
			if (data != null || data.hasArrears()) {
				return false;
			}
			if (goodsTypeEntry.value > 0) {
				paidSum += data.getCostToBuy(goodsTypeEntry.value);
			}
		}
		return player.hasGold(paidSum);
	}
	
	public MarketData payArrears(Player player, GoodsType goodsType) {
		MarketData md = marketGoods.getByIdOrNull(goodsType.getId());
		
		if (player.hasNotGold(md.getArrears())) {
			throw new IllegalStateException(
					"has not enought gold(" + player.getGold() + ") " + 
			        "to pay arrears(" + md.getArrears() + ") " + 
					"for " + md.getGoodsType());
		}
		player.subtractGold(md.getArrears());
		md.repayArrears();
		return md;
	}
	
	public void createArrears(GoodsType goodsType) {
	    MarketData md = marketGoods.getByIdOrNull(goodsType.getId());
		md.setArrears(md.getSalePrice() * Specification.options.getIntValue(GameOptions.ARREARS_FACTOR));
	}
	
	public void repayAllArrears() {
		for (MarketData md : marketGoods.entities()) {
			if (md.hasArrears()) {
				md.repayArrears();
			}
		}
	}
	
	public int arrearsCount() {
		int count = 0;
		for (MarketData md : marketGoods.entities()) {
			if (md.hasArrears()) {
				count++;
			}
		}
		return count;
	}
	
    private static TransactionEffectOnMarket TRANSACTION_EFFECT_ON_MARKET = new TransactionEffectOnMarket();

	public TransactionEffectOnMarket buyGoods(Game game, Player player, GoodsType goodsType, int goodsAmount) {
		return buyGoods(game, player, goodsType, goodsAmount, marketGoodsPrice);
	}

	public TransactionEffectOnMarket buyGoodsForBuilding(Game game, Player player, GoodsType goodsType, int goodsAmount) {
		return buyGoods(game, player, goodsType, goodsAmount, buildingGoodsPrice);
	}
	
	private TransactionEffectOnMarket buyGoods(
			Game game, Player player, 
			GoodsType goodsType, int goodsAmount, 
			GoodsPriceSpecification priceSpecification
	) {
		TRANSACTION_EFFECT_ON_MARKET.reset();
		TRANSACTION_EFFECT_ON_MARKET.goodsTypeId = goodsType.getId();
		TRANSACTION_EFFECT_ON_MARKET.quantity = goodsAmount;
       
        TRANSACTION_EFFECT_ON_MARKET.grossPrice = priceSpecification.price(goodsType, goodsAmount);
		
		if (player.hasNotGold(TRANSACTION_EFFECT_ON_MARKET.grossPrice)) {
			throw new IllegalStateException("Insufficient funds to pay for build");
		}
		player.subtractGold(TRANSACTION_EFFECT_ON_MARKET.grossPrice);
		
		if (goodsType.isStorable()) {
			int playerModifiedMarketAmount = goodsAmount;
			
		    MarketData marketData = marketGoods.getById(goodsType.getId());
			if (marketData.hasArrears()) {
				throw new IllegalStateException("can not buy goods: " + goodsType + " because of arrears");
			}
			playerModifiedMarketAmount = (int)player.nationType().applyModifier(Modifier.TRADE_BONUS, (float)goodsAmount);
			TRANSACTION_EFFECT_ON_MARKET.setPricesBeforeTransaction(marketData);
			marketData.modifyOnBuyGoods(goodsAmount, TRANSACTION_EFFECT_ON_MARKET.grossPrice, playerModifiedMarketAmount);
			TRANSACTION_EFFECT_ON_MARKET.setPricesAfterTransaction(marketData);
			
			// bought goods amount on market. Can be modified by player modifiers and differ from argument goodsAmount
			propagateTransactionToEuropeanMarkets(game, player, goodsType, -playerModifiedMarketAmount);
		}
		return TRANSACTION_EFFECT_ON_MARKET;
	}

	public TransactionEffectOnMarket sellGoods(Game game, Player player, GoodsType goodsType, int goodsAmount) {
	    MarketData marketData = marketGoods.getByIdOrNull(goodsType.getId());
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
        marketTransactionLogger.logSale(TRANSACTION_EFFECT_ON_MARKET);
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
        amount *= Randomizer.instance().randomInt(PROPAGATED_GOODS_LOWER_BOUND, PROPAGATED_GOODS_UPPER_BOUND);
        amount /= 100;
		return amount;
	}
	
    public boolean addGoodsToMarket(GoodsType goodsType, int amount) {
        MarketData data = marketGoods.getByIdOrNull(goodsType.getId());
        data.modifyAmountInMarket(amount);
        return data.price();
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
	
    /**
     * Get the most valuable goods available in one of the player's
     * colonies for the purposes of choosing a threat-to-boycott.  The
     * goods must not currently be boycotted, the player must have
     * traded in it, and the amount to be discarded will not exceed
     * GoodsContainer.CARGO_SIZE.
     * @param man 
     *
     * @return A goods object, or null if nothing suitable found.
     */
    public void findMostValuableGoods(Player player, MonarchActionNotification man) {
        if (!player.isEuropean()) {
        	man.setGoodsType(null);
        	return;
        }
        int value = 0;
        
        for (Settlement settlement : player.settlements.entities()) {
        	Colony colony = settlement.asColony();
        	for (MarketData md : marketGoods.entities()) {
        		if (md.hasArrears()) {
        			continue;
        		}
        		int a = colony.getGoodsContainer().goodsAmount(md.getGoodsType());
        		a = Math.min(a, ProductionSummary.CARRIER_SLOT_MAX_QUANTITY);
        		if (a <= 0) {
        			continue;
        		}
        		int sellPrice = md.getCostToSell(a);
        		if (sellPrice > value) {
        			value = sellPrice;
        			man.setGoodsAmount(a);
        			man.setGoodsType(md.getGoodsType());
        			man.setColonyId(colony.getId());
        		}
        	}
        }
    }

    public void initGoods() {
        for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
        	if (!goodsType.isStorable()) {
        		continue;
        	}
            MarketData marketData = marketGoods.getByIdOrNull(goodsType.getId());
            if (marketData == null) {
                marketData = new MarketData(goodsType);
                marketData.update();
                marketGoods.add(marketData);
            }
        }
    }
	
	public void setMarketTransactionLogger(MarketTransactionLogger marketTransactionLogger) {
		this.marketTransactionLogger = marketTransactionLogger;
	}
    
	public static class Xml extends XmlNodeParser<Market> {
		public Xml() {
			addNodeForMapIdEntities("marketGoods", MarketData.class);
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			String id = attr.getStrAttribute(ATTR_ID);
			Market market = new Market(id);
			nodeObject = market;
		}

		@Override
		public void startWriteAttr(Market node, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(node);
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
