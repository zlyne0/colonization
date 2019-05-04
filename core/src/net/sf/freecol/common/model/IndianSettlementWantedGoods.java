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
    private static final int GOODS_BASE_PRICE = 12;

    private final ProductionSummary maxProduction = new ProductionSummary();
	private final ProductionSummary consumptionGoods = new ProductionSummary();
	private UnitRole militaryRole = null; 
	private final List<GoodsType> goodsTypeOrder;
	private final ObjectIntMap<GoodsType> prices;

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
		calculateMaximumProduction(map, settlement);
		calculateGoodsConsumption(settlement);
		determineOwnerMilitaryRole(settlement);
		
		goodsTypeOrder.clear();
		prices.clear();
		
		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
			// The natives do not trade military or non-storable goods.
			if (!goodsType.isStorable() || goodsType.isMilitary()) {
				continue;
			}
			int priceToBuy = goodsPriceToBuy(settlement, goodsType);
			if (priceToBuy > ProductionSummary.CARRIER_SLOT_MAX_QUANTITY * TRADE_MINIMUM_PRICE) {
				prices.put(goodsType, priceToBuy);
				goodsTypeOrder.add(goodsType);
			}
		}
		
		Collections.sort(goodsTypeOrder, goodsTypePriceComparator);
		
		settlement.wantedGoods.clear();
		for (int i=0; i<goodsTypeOrder.size() && i < MAX_WANTED_GOODS; i++) {
			settlement.wantedGoods.add(goodsTypeOrder.get(i));
		}
	}

    private int goodsPriceToBuy(IndianSettlement is, GoodsType goodsType) {
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
        int valued = (retain <= current) ? 0 : Math.min(ProductionSummary.CARRIER_SLOT_MAX_QUANTITY, retain - current);
		
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
		
		for (Tile claimableTile : map.neighbourTiles(settlement.tile, settlement.settlementType.getClaimableRadius())) {
			// own tile or tile without owner
			if (claimableTile.getOwningSettlementId() == null || claimableTile.getOwningSettlementId().equals(settlement.getId())) {
				claimableTile.getType().productionInfo.addUnattendedProductionToSummary(maxProduction);
			}
		}
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
