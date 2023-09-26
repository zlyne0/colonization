package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.Randomizer;

public class IndianSettlementProduction {

    static class GoodsAmountPrice extends AbstractGoods {
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

	/**
	 * The amount of raw material that should be available before
	 * producing manufactured goods.
	 */
	public static final int KEEP_RAW_MATERIAL = 50;

    /** The production fudge factor. */
    public static final double NATIVE_PRODUCTION_EFFICIENCY = 0.67;
    
    private static final int MAX_GOODS_TYPE_TO_SELL = 3;
    
	private static final int GOODS_BASE_PRICE = 12;
	
	/** Do not sell less than this amount of goods. */
	private static final int TRADE_MINIMUM_SIZE = 20;
	
    private final ProductionSummary maxProduction = new ProductionSummary();
	private final ProductionSummary consumptionGoods = new ProductionSummary();
	private UnitRole militaryRole = null;
	private int productionTiles = 0;

	
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
				need += UnitRole.countRequiredGoodsToChangeRole(goodsType, unit, militaryRole);
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
    	int capacity = is.warehouseCapacity();
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
		productionTiles = 0;
		
        for (Tile claimableTile : map.neighbourTiles(settlement.tile, settlement.settlementType.getClaimableRadius())) {
            // own tile or tile without owner
            if (!claimableTile.isOccupiedForPlayer(settlement.getOwner())
				&& (claimableTile.getOwningSettlementId() == null || claimableTile.isOwnBySettlement(settlement))
            ) {
                claimableTile.getType().productionInfo.addUnattendedProductionToSummary(maxProduction);
                productionTiles++;
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
	
    public int goodsPriceToSell(IndianSettlement is, GoodsType goodsType, int amount) {
    	final int full = GOODS_BASE_PRICE + is.settlementType.getTradeBonus();
    	
		if (amount > 100) {
			throw new IllegalArgumentException("amount can be more then 100 but is is " + amount);
		}
    	
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
		java.util.Map<String, GoodsAmountPrice> goodsTypeOrders = new HashMap<>();

		for (Entry<String> goodsEntry : settlement.getGoodsContainer().entries()) {
			GoodsType goodsType = Specification.instance.goodsTypes.getById(goodsEntry.key);
			if (goodsType.isFood() && !goodsType.equalsId(GoodsType.FOOD)) {
				goodsType = Specification.instance.goodsTypes.getById(GoodsType.FOOD);
			}
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
			goodsTypeOrders.put(goodsType.getId(), new GoodsAmountPrice(goodsType, amount, price));
		}
		List<GoodsAmountPrice> goodsTypeOrder = new ArrayList<GoodsAmountPrice>(goodsTypeOrders.values());
		Collections.sort(goodsTypeOrder, exportGoodsComparator);
		return goodsTypeOrder.subList(0, Math.min(MAX_GOODS_TYPE_TO_SELL, goodsTypeOrder.size()));
	}

	void createInitialGoods(Map map, IndianSettlement settlement) {
	    Randomizer randomizer = Randomizer.instance();
	    
        calculateMaximumProduction(map, settlement);
	    
        int capacity = settlement.warehouseCapacity();
        
        StringBuilder logStr = new StringBuilder();
        logStr.append("capacity " + capacity).append("\n");
        
        for (Entry<String> prodGoods : maxProduction.entries()) {
            int stock = prodGoods.value * (10 + randomizer.randomInt(4) + settlement.settlementType.getTradeBonus());
            settlement.getGoodsContainer().increaseGoodsQuantity(
                prodGoods.key, 
                Math.min(stock, capacity)
            );
            
            logStr.append("" + prodGoods.key + " " + prodGoods.value + ", stock = " + stock ).append("\n");
            
            GoodsType goodsType = Specification.instance.goodsTypes.getById(prodGoods.key);
            GoodsType makes = goodsType.getMakes();
            if (makes != null && makes.isStorable() && !makes.isMilitary() && makes.isNewWorldOrigin()) {
                int makesVal = stock * (randomizer.randomInt(20, 30) + settlement.settlementType.getTradeBonus()) / 100;
                
                logStr.append("  add " + makes.getId() + " " + makesVal).append("\n");
                
                settlement.getGoodsContainer().increaseGoodsQuantity(
                    makes.getId(), 
                    Math.min(stock, makesVal)
                );
            }
        }
	    
        //System.out.println("" + logStr);
	}

	public void updateSettlementGoodsProduction(IndianSettlement is) {
	    ProductionSummary prod = maxProduction.cloneGoods();
	    
	    for (Entry<String> goodsEntry : prod.entries()) {
            if (!GoodsType.isFoodGoodsType(goodsEntry.key) && goodsEntry.value > 0) {
                // Raw production is too generous, apply a fudge factor to reduce it
                // a bit for the non-food cases.
                prod.decreaseToRatio(goodsEntry.key, NATIVE_PRODUCTION_EFFICIENCY);
            }
            
            if (productionTiles > is.getUnits().size()) {
                prod.decreaseToRatio(goodsEntry.key, (double)is.getUnits().size() / productionTiles);
            }
        }
	    is.tile.getType().productionInfo.addUnattendedProductionToSummary(prod);
	    
		for (Entry<String> goodsEntry : prod.entries()) {
            GoodsType goodsType = Specification.instance.goodsTypes.getById(goodsEntry.key);
            is.getGoodsContainer().increaseGoodsQuantity(goodsType.getStoredAs(), goodsEntry.value);
        }
		is.getGoodsContainer().decreaseGoodsToMinZero(consumptionGoods);
		is.getGoodsContainer().removeAbove(is.warehouseCapacity());
	}

	public void updateSettlementPopulationGrowth(Game game, IndianSettlement settlement) {
		int foodAmount = settlement.getGoodsContainer().goodsAmount(GoodsType.FOOD);
		boolean reduced = reducePopulation(game, settlement, foodAmount);
		if (reduced) {
			return;
		}
		increasePopulation(settlement, foodAmount);
	}

	private void increasePopulation(IndianSettlement settlement, int foodAmount) {
		if (settlement.getUnits().size() >= settlement.settlementType.getMaximumSize()) {
			return;
		}
		int rumAmount = settlement.getGoodsContainer().goodsAmount(GoodsType.RUM);
		if (foodAmount + 4 * rumAmount < Settlement.FOOD_PER_COLONIST + KEEP_RAW_MATERIAL) {
			return;
		}
		List<UnitType> unitTypes = Specification.instance.findUnitTypesWithAbility(Ability.BORN_IN_INDIAN_SETTLEMENT);
		if (unitTypes.isEmpty()) {
			return;
		}

		UnitType brave = Randomizer.instance().randomMember(unitTypes);
		Unit unit = new Unit(Game.idGenerator.nextId(Unit.class), brave, brave.getDefaultRole(), settlement.owner);
		settlement.owner.units.add(unit);
		unit.setIndianSettlement(settlement);
		unit.changeUnitLocation(settlement);

		settlement.consume(GoodsType.FOOD, Settlement.FOOD_PER_COLONIST);
		settlement.consume(GoodsType.RUM, KEEP_RAW_MATERIAL);
	}

	private boolean reducePopulation(Game game, IndianSettlement settlement, int foodAmount) {
		if (foodAmount > 0) {
			return false;
		}
		if (settlement.getUnits().isNotEmpty()) {
			Unit first = settlement.getUnits().first();
			first.removeFromLocation();
			settlement.owner.removeUnit(first);
		}
		if (settlement.getUnits().isEmpty()) {
			settlement.removeFromMap(game);
			settlement.removeFromPlayer();
		}
		return true;
	}

	public ProductionSummary getMaxProduction() {
		return maxProduction;
	}

	public ProductionSummary getConsumptionGoods() {
		return consumptionGoods;
	}
}
