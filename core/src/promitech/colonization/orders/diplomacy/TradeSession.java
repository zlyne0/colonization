package promitech.colonization.orders.diplomacy;

import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.IndianSettlementProduction;
import net.sf.freecol.common.model.IndianSettlementWantedGoods;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Tension.Level;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.Randomizer;

public class TradeSession {
	
    public static final int NO_TRADE_GOODS = 0,
                            NO_TRADE = -1,
                            NO_TRADE_HAGGLE = -2,
                            NO_TRADE_HOSTILE = -3;
	
	public final IndianSettlement indianSettlement; 
	public final Unit unit;
	
	private boolean canBuy = true;
	private boolean canSell = true;
	private boolean canGift = true;
	private boolean atWar = false;
	
	private final IndianSettlementProduction isProd = new IndianSettlementProduction();
	private final Map map;
	private int haggleCount = 0;
	
	public TradeSession(Map map, IndianSettlement indianSettlement, Unit tradeUnit) {
		this.map = map;
		this.indianSettlement = indianSettlement;
		this.unit = tradeUnit;
		atWar = indianSettlement.getOwner().atWarWith(unit.getOwner());
	}
	
	public void determineTradeChoices() {
		isProd.init(map, indianSettlement);
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
		haggleCount++;
		
		// TODO: modyfikatory np ze statek
		
		if (initialAskGold < 0 || price == initialAskGold) {
			return price;
		}
		
		if (haggleCount > 1 && Randomizer.instance().randomInt(3 + haggleCount) >= 3) {
			return NO_TRADE_HAGGLE;
		}
		
		return initialAskGold;
	}
	
	public void acceptSellOfferToIndianSettlement(GoodsType goodsType, int amount, int price) {
		unit.getGoodsContainer().moveGoods(goodsType, amount, indianSettlement.getGoodsContainer());
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
		unit.getGoodsContainer().moveGoods(goodsType, amount, indianSettlement.getGoodsContainer());
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
	
	public boolean isCanBuy() {
		// TODO: can free space
		return canBuy && !atWar;
	}

	public boolean isCanSell() {
		return canSell && !atWar && unit.hasGoodsCargo();
	}

	public boolean isCanGift() {
		return canGift && unit.hasGoodsCargo();
	}
}
