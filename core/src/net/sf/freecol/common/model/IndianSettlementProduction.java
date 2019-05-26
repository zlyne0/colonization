package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;

public class IndianSettlementProduction {

    class GoodsAmountPrice extends AbstractGoods {
    	private final GoodsType type;
		private int price;
		
		public GoodsAmountPrice(GoodsType goodsType, int amount, int price) {
			super(goodsType.getId(), amount);
			this.type = goodsType;
			this.price = price;
		}
		
		@Override
		public String toString() {
			return type.getId() + " " + getQuantity() + " for " + price;
		}

		public GoodsType getType() {
			return type;
		}

		public int getPrice() {
			return price;
		}
    }
	
    private static final int MAX_GOODS_TYPE_TO_SELL = 3;
    
	private static final int GOODS_BASE_PRICE = 12;
	
	/** Do not sell less than this amount of goods. */
	private static final int TRADE_MINIMUM_SIZE = 20;
	
    private final ProductionSummary maxProduction = new ProductionSummary();
	private final ProductionSummary consumptionGoods = new ProductionSummary();
	private UnitRole militaryRole = null; 

	
    // When choosing what goods to sell, sort goods with new world
    // goods first, then by price, then amount.
    private final Comparator<GoodsAmountPrice> exportGoodsComparator = new Comparator<GoodsAmountPrice>() {
        public int compare(GoodsAmountPrice goods1, GoodsAmountPrice goods2) {
            int cmp;
            GoodsType t1 = goods1.getType();
            GoodsType t2 = goods2.getType();
            cmp = (((t2.isNewWorldGoodsType()) ? 1 : 0) - ((t1.isNewWorldGoodsType()) ? 1 : 0));
            if (cmp == 0) {
                int a1 = goods1.getQuantity();
                int a2 = goods2.getQuantity();
                cmp = goods2.price - goods1.price;
                if (cmp == 0) {
                    cmp = a2 - a1;
                }
            }
            return cmp;
        }
    };
	
	public IndianSettlementProduction() {
		
	}
	
	public void init(Map map, IndianSettlement settlement) {
		calculateMaximumProduction(map, settlement);
		calculateGoodsConsumption(settlement);
		determineOwnerMilitaryRole(settlement);
	}
	
    /**
     * Calculates how much of the given goods type this settlement
     * wants and should retain.
     */
	private int getWantedGoodsAmount(IndianSettlement is, GoodsType goodsType) {
		if (is.getUnits().isEmpty()) {
			return 0;
		}
		
		if (goodsType.isMilitary()) {
			// Retain enough goods to fully arm.
			int need = 0;
			for (Unit unit : is.getUnits().entities()) {
				if (unit.getUnitRole().equalsId(militaryRole)) {
					continue;
				}
				need += UnitRoleLogic.countRequiredGoodsToChangeRole(goodsType, unit, militaryRole);
			}
			return need;
		}
		
        int consumption = consumptionGoods.getQuantity(goodsType.getId());
        if (goodsType.isFood()) {
            // Food is perishable, do not try to retain that much
            return Math.max(40, consumption * 3);
        }
        if (goodsType.isTradeGoods() || goodsType.isNewWorldOrigin() || goodsType.isRefined()) {
            // Aim for 10 years supply, resupply is doubtful
            return Math.max(80, consumption * 20);
        }
        // Just keep some around
        return 2 * is.getUnits().size();
	}

	public int goodsPriceToBuy(IndianSettlement is, GoodsType goodsType, int amount) {
		int price = 0;
		if (goodsType.isMilitary()) {
			price = militaryGoodsPriceToBuy(is, goodsType, amount);
		}
		if (price == 0) {
			price = normalGoodsPriceToBuy(is, goodsType, amount);
		}
		
		// Premium paid for wanted goods types
		int[] wantedGoodsBonusRatio = new int[] { 150, 125, 110 };
		int wantedBonus = 100;
		for (int i=0; i<is.wantedGoods.size() && i<wantedGoodsBonusRatio.length; i++) {
			if (is.wantedGoods.get(i).equalsId(goodsType)) {
				wantedBonus = wantedGoodsBonusRatio[i];
			}
		}
		
        final int wantedBase = 100; // Granularity for wanted bonus
        // Do not simplify with *=, we want the integer truncation.
        price = wantedBonus * price / wantedBase;
		return price;
	}
	
    private int militaryGoodsPriceToBuy(IndianSettlement is, GoodsType goodsType, int amount) {
    	int required = getWantedGoodsAmount(is, goodsType); 
    	if (required == 0) {
    		return 0;
    	}
    	final int full = GOODS_BASE_PRICE + is.settlementType.getTradeBonus();
    	
        // If the settlement can use more than half of the goods on offer,
        // then pay top dollar for the lot.  Otherwise only pay the premium
        // price for the part they need and refer the remaining amount to
        // the normal goods pricing.
    	int valued = Math.max(0, required - is.getGoodsContainer().goodsAmount(goodsType));
    	
    	int price = 0;
    	if (valued > amount / 2) {
    		price = full * amount;
    	} else {
    		price = valued * full + normalGoodsPriceToBuy(is, goodsType, amount - valued);
    	}
		return price;
	}

	public int normalGoodsPriceToBuy(IndianSettlement is, GoodsType goodsType, int amount) {
    	int capacity = is.getGoodsCapacity();
    	int current = is.getGoodsContainer().goodsAmount(goodsType);
    	
		if (goodsType.isRefined()) {
            int rawProduction = maxProduction.getQuantity(goodsType.getId());
            int add = (rawProduction < 5) ? 10 * rawProduction
                : (rawProduction < 10) ? 5 * rawProduction + 25
                : (rawProduction < 20) ? 2 * rawProduction + 55
                : 100;
            // Decrease bonus in proportion to current stock, up to capacity.
            add = add * Math.max(0, capacity - current) / capacity;
            current += add;
		} else if (goodsType.isTradeGoods()) {
	        final int tradeGoodsAdd = 20; // Fake additional trade goods present
			current += tradeGoodsAdd;
		}
		
        // Only interested in the amount of goods that keeps the
        // total under the threshold.
        int retain = Math.min(getWantedGoodsAmount(is, goodsType), capacity);
        int valued = (retain <= current) ? 0 : Math.min(amount, retain - current);
		
        // Unit price then is maximum price plus the bonus for the
        // settlement type, reduced by the proportion of goods present.
        int unitPrice = (GOODS_BASE_PRICE + is.settlementType.getTradeBonus())
            * Math.max(0, capacity - current) / capacity;
        
        // But farmed goods are always less interesting.
        // and small settlements are not interested in building.
        if (goodsType.isFarmed() || goodsType.isRawBuildingMaterial()) {
        	unitPrice /= 2;
        }

        // Only pay for the portion that is valued.
        return (unitPrice < 0) ? 0 : valued * unitPrice;
	}
	
	private void calculateGoodsConsumption(IndianSettlement settlement) {
		consumptionGoods.decreaseAllToZero();
		
		for (Unit unit : settlement.owner.units.entities()) {
			if (unit.isBelongToIndianSettlement(settlement)) {
				consumptionGoods.addGoods(unit.unitType.unitConsumption.entities());
			}
		}
	}
	
	private void calculateMaximumProduction(Map map, IndianSettlement settlement) {
		maxProduction.decreaseAllToZero();
		settlement.initMaxProduction(map, maxProduction);
	}

	private void determineOwnerMilitaryRole(IndianSettlement settlement) {
		militaryRole = null;
		if (settlement.getUnits().isEmpty()) {
			return;
		}
		Unit firstUnit = settlement.getUnits().first();
		for (UnitRole milUnitRole : Specification.instance.militaryRoles) {
			if (milUnitRole.isAvailableTo(settlement.getOwner().getFeatures(), firstUnit.unitType)) {
				militaryRole = milUnitRole;
			}
		}
	}
	
    public int goodsPriceToSell(IndianSettlement is, GoodsType goodsType, int amount) {
    	final int full = GOODS_BASE_PRICE + is.settlementType.getTradeBonus();
    	
        // Base price is purchase price plus delta.
        // - military goods at double value
        // - trade goods at +50%
        int price = amount + Math.max(0, 11 * goodsPriceToBuy(is, goodsType, amount) / 10);
        if (goodsType.isMilitary()) {
            price = Math.max(price, amount * full * 2);
        } else if (goodsType.isTradeGoods()) {
            price = Math.max(price, 150 * amount * full / 100);
        }
        return price;
    }
    
	public List<? extends AbstractGoods> goodsToSell(IndianSettlement settlement, Unit carrier) {
		List<GoodsAmountPrice> goodsTypeOrder = new ArrayList<GoodsAmountPrice>();
		
		for (Entry<String> goodsEntry : settlement.getGoodsContainer().entries()) {
			GoodsType goodsType = Specification.instance.goodsTypes.getById(goodsEntry.key);
			if (goodsType.isTradeGoods()) {
				continue;
			}
			int amount = goodsEntry.value;
			int retain = getWantedGoodsAmount(settlement, goodsType);
			if (retain >= amount) {
				continue;
			}
			amount -= retain;
			if (amount > ProductionSummary.CARRIER_SLOT_MAX_QUANTITY) {
				amount = ProductionSummary.CARRIER_SLOT_MAX_QUANTITY;
			}
			if (carrier != null) {
				amount = (int)carrier.unitType.applyModifier(Modifier.TRADE_VOLUME_PENALTY, amount);
			}
			if (amount < TRADE_MINIMUM_SIZE) {
				continue;			
			}
			int price = goodsPriceToSell(settlement, goodsType, amount);
			goodsTypeOrder.add(new GoodsAmountPrice(goodsType, amount, price));
		}
		Collections.sort(goodsTypeOrder, exportGoodsComparator);
		return goodsTypeOrder.subList(0, Math.min(MAX_GOODS_TYPE_TO_SELL, goodsTypeOrder.size()));
	}
	
}
