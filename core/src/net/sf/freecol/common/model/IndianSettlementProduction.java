package net.sf.freecol.common.model;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.specification.GoodsType;

public class IndianSettlementProduction {

    private static final int GOODS_BASE_PRICE = 12;
	
    private final ProductionSummary maxProduction = new ProductionSummary();
	private final ProductionSummary consumptionGoods = new ProductionSummary();
	private UnitRole militaryRole = null; 
	
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

	public int goodsPriceToBuyInTrade(IndianSettlement is, GoodsType goodsType, int amount) {
		int price = 0;
		if (goodsType.isMilitary()) {
			price = militaryGoodsPriceToBuy(is, goodsType, amount);
		}
		if (price == 0) {
			price = goodsPriceToBuy(is, goodsType, amount);
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
    		price = valued * full + goodsPriceToBuy(is, goodsType, amount - valued);
    	}
		return price;
	}

	public int goodsPriceToBuy(IndianSettlement is, GoodsType goodsType, int amount) {
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
}
