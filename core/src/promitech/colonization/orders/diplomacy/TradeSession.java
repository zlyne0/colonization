package promitech.colonization.orders.diplomacy;

import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap.Entries;

import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.IndianSettlementProduction;
import net.sf.freecol.common.model.IndianSettlementWantedGoods;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Tension.Level;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.Randomizer;
import promitech.colonization.ui.resources.StringTemplate;

public class TradeSession {
	
    public static final int NO_TRADE_GOODS = 0,
                            NO_TRADE = -1,
                            NO_TRADE_HAGGLE = -2,
                            NO_TRADE_HOSTILE = -3;
	
	public final IndianSettlement indianSettlement; 
	private final Unit unit;
	
	private boolean canBuy = true;
	private boolean canSell = true;
	private boolean canGift = true;
	private boolean atWar = false;
	
	private final IndianSettlementProduction isProd = new IndianSettlementProduction();
	private final Map map;
	private int sellHaggleCount = 0;
	private int buyHaggleCount = 0;
	
	public TradeSession(Map map, IndianSettlement indianSettlement, Unit tradeUnit) {
		this.map = map;
		this.indianSettlement = indianSettlement;
		this.unit = tradeUnit;
		atWar = indianSettlement.getOwner().atWarWith(unit.getOwner());
	}
	
	public TradeSession updateSettlementProduction() {
		isProd.init(map, indianSettlement);
		return this;
	}
	
	public int sellOffer(GoodsType goodsType, int amount) {
		return sellOffer(goodsType, amount, -1);
	}
	
	public int haggleSellOffer(GoodsType goodsType, int amount, int initialAskGold) {
		int hagglePrice = (initialAskGold * 11) / 10;
		int price = sellOffer(goodsType, amount, hagglePrice);
		if (price <= 0) {
			unit.reduceMovesLeftToZero();
			canSell = false;
		}
		System.out.println("tradeSession[" + unit.getOwner().getId() + "]"
			+ ".haggleSellOffer[" + indianSettlement.getId() + "]"
			+ " goods[" + goodsType.getId() + " " + amount + "] from " + initialAskGold + " to " + hagglePriceToStr(price)
		);
		return price;
	}
	
	private String hagglePriceToStr(int price) {
		switch (price) {
		case NO_TRADE_GOODS : return "NO_TRADE_GOODS";
		case NO_TRADE : return "NO_TRADE";
		case NO_TRADE_HAGGLE : return "NO_TRADE_HAGGLE";
		case NO_TRADE_HOSTILE : return "NO_TRADE_HOSTILE";
		default:
			return Integer.toString(price);
		}
	}
	
	private int sellOffer(GoodsType goodsType, int amount, int initialAskGold) {
		int price = isProd.goodsPriceToBuy(indianSettlement, goodsType, amount);
		
		Level level = indianSettlement.getTension(unit.getOwner()).getLevel();
		switch (level) {
		case HAPPY:
		case CONTENT:
			break;
		case DISPLEASED:
			price = price / 2;
			break;
		case ANGRY:
			if (!goodsType.isMilitary()) {
				return NO_TRADE_HOSTILE;  
			}
			price = price / 2;
			break;
		default:
			return NO_TRADE_HOSTILE;
		}
		if (price <= 0) {
			return NO_TRADE;
		}
		sellHaggleCount++;
		
		if (Specification.options.getBoolean(GameOptions.ENHANCED_MISSIONARIES) && indianSettlement.hasMissionary(unit.getOwner())) {
			Unit missionary = indianSettlement.getMissionary().getUnit();
			price = (int)missionary.unitType.applyModifier(Modifier.MISSIONARY_TRADE_BONUS, price);
			price = (int)missionary.unitRole.applyModifier(Modifier.MISSIONARY_TRADE_BONUS, price);
		}
		if (unit.isNaval()) {
			Modifier shipTradePenalty = Specification.instance.modifiers.getById(Modifier.SHIP_TRADE_PENALTY);
			price = (int)shipTradePenalty.apply(price);
		}		
		if (initialAskGold < 0 || price == initialAskGold) {
			return price;
		}
		
		if (sellHaggleCount > 1 && Randomizer.instance().randomInt(3 + sellHaggleCount) >= 3) {
			return NO_TRADE_HAGGLE;
		}
		
		return initialAskGold;
	}
	
	public void acceptSellOfferToIndianSettlement(GoodsType goodsType, int amount, int price) {
		unit.getGoodsContainer().transferGoods(goodsType, amount, indianSettlement.getGoodsContainer());
		indianSettlement.getOwner().transferGoldToPlayer(price, unit.getOwner());
		indianSettlement.modifyTensionWithOwnerTension(unit.getOwner(), -amount / 500);
		unit.reduceMovesLeftToZero();
		canSell = false;
		
		IndianSettlementWantedGoods wg = new IndianSettlementWantedGoods();
		wg.updateWantedGoods(map, indianSettlement);
		
		System.out.println(
			"tradeSession[" + unit.getOwner().getId() + "]"
			+ ".sellGoodsTo[" + indianSettlement.getId() + "]"
			+ " " + goodsType.getId() + " " + amount + " for " + price + " gold"
		);
	}

	public void deliverGoodsToIndianSettlement(GoodsType goodsType, int amount) {
		int price = isProd.goodsPriceToBuy(indianSettlement, goodsType, amount);
		unit.getGoodsContainer().transferGoods(goodsType, amount, indianSettlement.getGoodsContainer());
		unit.reduceMovesLeftToZero();
		canGift = false;
		
		indianSettlement.modifyTensionWithOwnerTension(unit.getOwner(), -price / 50);
		
		IndianSettlementWantedGoods wg = new IndianSettlementWantedGoods();
		wg.updateWantedGoods(map, indianSettlement);
		
		System.out.println(
			"tradeSession[" + unit.getOwner().getId() + "]"
			+ ".deliverGoodsTo[" + indianSettlement.getId() + "]"
			+ " " + goodsType.getId() + " " + amount
		);
	}

	public int buyOfferPrice(GoodsType goodsType, int amount) {
		return buyOfferPrice(goodsType, amount, -1);
	}
	
	public int haggleBuyOfferPrice(GoodsType goodsType, int amount, int price) {
		int newPrice = price * 9 / 10;
		int haggledPrice = buyOfferPrice(goodsType, amount, newPrice);
		if (haggledPrice <= 0) {
			unit.reduceMovesLeftToZero();
			canBuy = false;
		}
		System.out.println("tradeSession[" + unit.getOwner().getId() + "]"
			+ ".haggleBuyOffer[" + indianSettlement.getId() + "]"
			+ " goods[" + goodsType.getId() + " " + amount + "] from " + price + " to " + hagglePriceToStr(haggledPrice)
		);
		return haggledPrice;
	}
	
	private int buyOfferPrice(GoodsType goodsType, int amount, int initialPrice) {
		if (initialPrice == -1) {
			int price = isProd.goodsPriceToSell(indianSettlement, goodsType, amount);
			Level tensionLevel = indianSettlement.getTension(unit.getOwner()).getLevel();
			switch (tensionLevel) {
			case HAPPY:
			case CONTENT:
				break;
			case DISPLEASED:
				price = price * 2;
				break;
			default:
				return NO_TRADE_HOSTILE;
			}
			
			int notModifiedPrice = price;
			if (Specification.options.getBoolean(GameOptions.ENHANCED_MISSIONARIES) && indianSettlement.hasMissionary(unit.getOwner())) {
				// lower price for missionary
				Unit missionary = indianSettlement.getMissionary().getUnit();
				price = (int)missionary.unitType.applyModifier(Modifier.MISSIONARY_TRADE_BONUS, price);
				price = (int)missionary.unitRole.applyModifier(Modifier.MISSIONARY_TRADE_BONUS, price);
			}
			if (unit.isNaval()) {
				// higher price for naval
				Modifier shipTradePenalty = Specification.instance.modifiers.getById(Modifier.SHIP_TRADE_PENALTY);
				price = (int)shipTradePenalty.apply(price);
			}		
			price = notModifiedPrice - ( price - notModifiedPrice);
			return price;
		}
		buyHaggleCount++;
		
		if (buyHaggleCount > 1 && Randomizer.instance().randomInt(3 + buyHaggleCount) >= 3) {
			return NO_TRADE_HAGGLE;
		}
		return initialPrice;
	}
	
	public void acceptBuyOffer(GoodsType goodsType, int amount, int price) {
		if (unit.getOwner().hasNotGold(price)) {
			throw new IllegalStateException("player " + unit.getOwner() + " has not " + price + " gold");
		}
		indianSettlement.getGoodsContainer().transferGoods(goodsType, amount, unit.getGoodsContainer());

		unit.getOwner().transferGoldToPlayer(price, indianSettlement.getOwner());
		
		indianSettlement.modifyTensionWithOwnerTension(unit.getOwner(), -price / 50);
		unit.reduceMovesLeftToZero();
		canSell = false;
		
		IndianSettlementWantedGoods wg = new IndianSettlementWantedGoods();
		wg.updateWantedGoods(map, indianSettlement);
		
		System.out.println(
			"tradeSession[" + unit.getOwner().getId() + "]"
			+ ".buyGoodsFrom[" + indianSettlement.getId() + "]"
			+ " " + goodsType.getId() + " " + amount + " for " + price + " gold"
		);
	}
	
	public boolean tradeUnitHasNotGold(int price) { 
		return unit.getOwner().hasNotGold(price);
	}
	
	public Entries<String> goodsToSell() {
		// sell by unit
		return unit.getGoodsContainer().entries();
	}
	
	public Entries<String> goodsToDeliver() {
		// deliver by unit
		return unit.getGoodsContainer().entries();
	}
	
	public List<? extends AbstractGoods> goodsToBuy() {
		// buy from settlement
		return isProd.goodsToSell(indianSettlement, unit);
	}
	
	public boolean canBuy() {
		return canBuy && !atWar && unit.hasSpaceForAdditionalCargo();
	}

	public void markNoBuy() {
		canBuy = false;
	}

	public boolean canSell() {
		return canSell && !atWar && unit.hasGoodsCargo();
	}

	public void markNoSell() {
		canSell = false;
	}

	public boolean canGift() {
		return canGift && unit.hasGoodsCargo();
	}
	
	public StringTemplate traderNationName() {
		return unit.getOwner().getNationName();
	}
}
