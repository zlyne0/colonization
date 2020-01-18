package net.sf.freecol.common.model.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.ProductionLocation;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitConsumption;
import net.sf.freecol.common.model.specification.GoodsType;

public class ColonyPlan {

	enum AssignStatus {
		OK, 
		NO_LOCATION, 
		NO_FOOD; // can not produce more food
	}
	
	public enum Plan {
		// only food
		Food(GoodsType.GRAIN, GoodsType.FISH),
		Bell(GoodsType.BELLS),
		Building("model.goods.hammers"),
		MostValueble(),
		Tools("model.goods.tools"),
		Muskets(GoodsType.MUSKETS); 
		
		private final Set<String> prodGoodsId = new HashSet<String>();
		private final String[] prodGoodsIdsArray;
		
		private Plan(String ... prodGoodsIds) {
			this.prodGoodsIdsArray = prodGoodsIds;
			for (String id : prodGoodsIdsArray) {
				this.prodGoodsId.add(id);
			}
		}
		
		public static Plan of(String str) {
			for (Plan p : Plan.values()) {
				if (p.name().equalsIgnoreCase(str)) {
					return p;
				}
			}
			throw new IllegalArgumentException("can not find Plan enum value by string: " + str);
		}
		
		public boolean contains(String goodId) {
			return goodId != null && prodGoodsId.contains(goodId);
		}
		
		
	}
	
    private final ProductionSummary prod = new ProductionSummary(); 
    private final ProductionSummary ingredients = new ProductionSummary();
    private final List<String> goodsTypeGoalIngredientsChain = new ArrayList<String>();
    
	private boolean consumeWarehouseResources = false;
	
	private final Colony colony;
    private Plan[] plans;
    private int nextPlanIndex = 0;
    
	public ColonyPlan(Colony colony) {
		this.colony = colony;
	}
	
	class GoodsMaxProductionLocationWithUnit {
		private int score = -1;
		private Unit worker;
		private GoodsType goodsType;
		private ProductionLocation location;
		private Map<Unit, GoodMaxProductionLocation> ingredientsWorkersAllocation = new HashMap<Unit, GoodMaxProductionLocation>();
		
		GoodsMaxProductionLocationWithUnit() {
		}
		
		public void update(
			int score, Unit worker, GoodsType goodsType, ProductionLocation location,
			Map<Unit, GoodMaxProductionLocation> ingredientsWorkersAllocation
		) {
			if (score > this.score) {
				this.score = score;
				this.worker = worker;
				this.goodsType = goodsType;
				this.location = location;
				this.ingredientsWorkersAllocation.clear();
				this.ingredientsWorkersAllocation.putAll(ingredientsWorkersAllocation);
			}
		}
		
		public boolean isEmpty() {
			return score < 0;
		}
		
		public String toString() {
			String st = worker.unitType.getId() + 
					", scrore=" + score + 
					", goodsType=" + goodsType + 
					", location=" + location.getId() + "\n";
			for (java.util.Map.Entry<Unit, GoodMaxProductionLocation> entry : ingredientsWorkersAllocation.entrySet()) {
				st += "  " + entry.getKey().getId() + " " + entry.getKey().unitType 
					+ " " + entry.getValue().getProductionLocation().getId() + "\n";
			}
			return st;
		}

		public boolean hasBetterNewScore(int newScore) {
			return newScore > score;
		}
	}
	
	/**
	 * 
	 * @return boolean - return true when assign workers
	 */
	private boolean mostValueable(List<Unit> availableWorkers) {
		GoodsMaxProductionLocationWithUnit max = new GoodsMaxProductionLocationWithUnit();
		theMostValuableForWorkers(availableWorkers, max);
		if (!max.isEmpty()) {
			addWorkerToProductionLocation(max.worker, max.location, max.goodsType);
			
			availableWorkers.remove(max.worker);
			
			for (java.util.Map.Entry<Unit, GoodMaxProductionLocation> entry : max.ingredientsWorkersAllocation.entrySet()) {
				Unit worker = entry.getKey();
				addWorkerToProductionLocation(
					worker, 
					entry.getValue().getProductionLocation(),
					entry.getValue().getGoodsType()
				);
				availableWorkers.remove(worker);
			}
		}
		return !max.isEmpty();
	}
	
	private void theMostValuableForWorkers(List<Unit> availableWorkers, GoodsMaxProductionLocationWithUnit max) {
		Map<Unit, GoodMaxProductionLocation> ingredientsWorkersAllocation = new HashMap<Unit, GoodMaxProductionLocation>();
		
		for (Unit worker : availableWorkers) {
    		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
    			if (!isLuxery(goodsType)) {
    				continue;
    			}
    			if (colony.getOwner().market().hasArrears(goodsType)) {
    				continue;
    			}
    			Building building = findBuildingByGoodsType(goodsType);
    			if (building == null || !building.canAddWorker(worker)) {
    				continue;
    			}
    			
    			createPlanProductionChain(goodsType);
    			prod.makeEmpty();
    			ingredients.makeEmpty();
    			building.determineMaxPotentialProduction(colony, worker, prod, ingredients, goodsType.getId());
    			int produceAmount = prod.getQuantity(goodsType.getId());
    			
    			produceAmount = colony.maxGoodsAmountToFillWarehouseCapacity(goodsType.getId(), produceAmount);
    			// first calculate score to avoid production check because it is heavy
    			int score = colony.getOwner().market().getSalePrice(goodsType, produceAmount);
    			if (max.hasBetterNewScore(score)) {
    				if (canProduce(worker, without(availableWorkers, worker), ingredients, ingredientsWorkersAllocation)) {
    					max.update(score, worker, goodsType, building, ingredientsWorkersAllocation);
    				}
    			}
			}
		}
	}

	private boolean canProduce(
		Unit worker, 
		List<Unit> avalWorkers, 
		ProductionSummary ingredientsToDelivere, 
		Map<Unit, GoodMaxProductionLocation> ingredientsWorkersAllocation 
	) {
		ingredientsWorkersAllocation.clear();
		
		if (hasGoodsToConsume(ingredientsToDelivere)) {
			if (!canSustainWorkers(1, 0)) {
				if (avalWorkers.isEmpty()) {
					return false;
				}
				Unit foodWorker = workersByPriorityToPlan(avalWorkers, GoodsType.GRAIN, GoodsType.FISH);
				GoodMaxProductionLocation foodBestLocation = theBestLocation(foodWorker, GoodsType.GRAIN, GoodsType.FISH);
				if (foodBestLocation == null) {
					return false;
				}
				ingredientsWorkersAllocation.put(foodWorker, foodBestLocation);
				return canSustainWorkers(2, foodBestLocation.getProduction());
			}
			return true;
		}
		if (avalWorkers.isEmpty()) {
			return false;
		}
		Unit worker2 = workersByPriorityToPlan(avalWorkers, ingredientsToDelivere.singleEntry().key);
		GoodMaxProductionLocation theBestLocation = theBestLocation(worker2, ingredientsToDelivere.singleEntry().key);
		if (theBestLocation == null || theBestLocation.getProduction() < ingredientsToDelivere.singleEntry().value) {
			return false;
		}
		ingredientsWorkersAllocation.put(worker2, theBestLocation);
		
		// can sustain worker and worker2 
		if (canSustainWorkers(2, 0)) {
			return true;
		}
		
		// has worker to produce food
		if (avalWorkers.size() -1 <= 0) {
			return false;
		}
		Unit foodWorker = workersByPriorityToPlan(without(avalWorkers, worker2), GoodsType.GRAIN, GoodsType.FISH);
		GoodMaxProductionLocation foodBestLocation = theBestLocation(foodWorker, GoodsType.GRAIN, GoodsType.FISH);
		if (foodBestLocation == null) {
			return false;
		}
		ingredientsWorkersAllocation.put(foodWorker, foodBestLocation);
		return canSustainWorkers(3, foodBestLocation.getProduction());
	}

	private final Map<String,Building> buildingByGoodsType = new HashMap<String, Building>();
	
	private Building findBuildingByGoodsType(GoodsType goodsType) {
		if (!buildingByGoodsType.isEmpty()) {
			return buildingByGoodsType.get(goodsType.getId());
		}
		for (Building building : colony.buildings.entities()) {
			for (Production production : building.buildingType.productionInfo.getAttendedProductions()) {
				for (java.util.Map.Entry<GoodsType, Integer> entry : production.outputEntries()) {
					buildingByGoodsType.put(entry.getKey().getId(), building);
				}
			}
		}
		return buildingByGoodsType.get(goodsType.getId());
	}
	
	private boolean isLuxery(GoodsType goodsType) {
		return goodsType.equalsId("model.goods.rum") || 
			goodsType.equalsId("model.goods.cigars") ||
			goodsType.equalsId("model.goods.cloth") ||
			goodsType.equalsId("model.goods.coats");    			
	}
	
	public void execute2(Plan ... plans) {
		this.plans = plans;
		
		// -first- implemented scenario (BalancedProduction)
		// plan list means that assign one colonist per plan
		// -second- not implemented scenario (MaximizationProduction)
		// take plan and assign to them colonist to end of place or resources
		
        List<Unit> availableWorkers = new ArrayList<Unit>(colony.settlementWorkers().size());
        removeWorkersFromColony(availableWorkers);

        LinkedList<String[]> stos = new LinkedList<String[]>();
        int noPlanWorkCounter = 0;
        
        while (!availableWorkers.isEmpty()) {
        	if (noPlanWorkCounter > 10) {
        		// avoid infinite loop
        		return;
        	}
        	if (stos.isEmpty()) {
                Plan plan = nextPlan();
                createPlanProductionChain(plan);
                
                if (plan == Plan.MostValueble) {
                	boolean assignWorkers = mostValueable(availableWorkers);
                	if (assignWorkers) {
                		noPlanWorkCounter = 0;
                	} else {
                		noPlanWorkCounter++;
                	}
                	continue;
                }
                stos.add(goodsFromPlan(plan));
        	}
        	
        	String[] goodsTypeToProduce = stos.pop();
        	Unit worker = workersByPriorityToPlan(availableWorkers, goodsTypeToProduce);

        	if (lackOfIngedients(goodsTypeToProduce, worker, stos)) {
        		noPlanWorkCounter++;
        		continue;
        	}
        	
        	GoodMaxProductionLocation location = theBestLocation(worker, goodsTypeToProduce);
        	if (location == null) {
        		noPlanWorkCounter++;
        		continue;
        	}
        	if (canSustainNewWorker(worker, location.getGoodsType(), location.getProduction())) {
        		addWorkerToProductionLocation(worker, location.getProductionLocation(), location.getGoodsType());
        		availableWorkers.remove(worker);
        		noPlanWorkCounter = 0;
        	} else {
        		stos.addFirst(Plan.Food.prodGoodsIdsArray);
        	}
        }
	}
	
	private String[] goodsFromPlan(Plan plan) {
		if (plan == Plan.MostValueble) {
			return plan.prodGoodsIdsArray;
		}
		return plan.prodGoodsIdsArray;
	}
	
	private boolean lackOfIngedients(String[] goodsTypesToProduce, Unit worker, LinkedList<String[]> goodsBuildingQueue) {
        prod.makeEmpty();
        ingredients.makeEmpty();
        for (String goodsTypeId : goodsTypesToProduce) {
            colony.determineMaxPotentialProduction(goodsTypeId, worker, prod, ingredients);
        }
        boolean lackOfIngredients = false;
        for (Entry<String> ingredient : ingredients.entries()) {
            if (!hasGoodsToConsume(ingredient.key, ingredient.value)) {
                lackOfIngredients = true;
                goodsBuildingQueue.addFirst(new String[] { ingredient.key });
            }
        }
	    return lackOfIngredients;
	}
	
	private void removeWorkersFromColony(List<Unit> availableWorkers) {
		for (Unit unit : colony.settlementWorkers()) {
			unit.changeUnitLocation(colony.tile);
			unit.canChangeState(UnitState.SKIPPED);
			availableWorkers.add(unit);
		}
		colony.updateModelOnWorkerAllocationOrGoodsTransfer();
		colony.updateColonyPopulation();
	}
	
    private void addWorkerToProductionLocation(Unit worker, ProductionLocation location, GoodsType goodsType) {
    	if (location instanceof ColonyTile) {
    		colony.addWorkerToTerrain((ColonyTile)location, worker, goodsType);
    	}
    	if (location instanceof Building) {
    		colony.addWorkerToBuilding((Building)location, worker);
    	}
        colony.updateModelOnWorkerAllocationOrGoodsTransfer();
        colony.updateColonyPopulation();
    }

	private boolean canSustainNewWorker(Unit worker, GoodsType goodsTypeToProduce, int produceAmount) {
		ProductionSummary productionSummary = colony.productionSummary();
		for (UnitConsumption unitConsumption : worker.unitType.unitConsumption.entities()) {
			if (unitConsumption.getTypeId().equals(GoodsType.BELLS)) {
				// do not care
				continue;
			}
			int prod = productionSummary.getQuantity(unitConsumption.getTypeId());
			// when unit produce what consume, unit can sustain himself
			if (GoodsType.isFoodGoodsType(unitConsumption.getTypeId()) && goodsTypeToProduce != null && goodsTypeToProduce.isFood()) {
		        prod += produceAmount;
			}
			
			// when consume food and is lack of food then it is possible to stop breeding horses and sustain colonist
			if (GoodsType.isFoodGoodsType(unitConsumption.getTypeId())) {
			    prod += productionSummary.getQuantity(GoodsType.HORSES);
			}
			if (unitConsumption.getQuantity() > prod) {
				return false;
			}
		}
		return true;
	}

    private boolean canSustainWorkers(int workersCount, int additionalFoodProduction) {
    	ProductionSummary productionSummary = colony.productionSummary();    	
    	int prod = productionSummary.getQuantity(GoodsType.FOOD);
    	prod += productionSummary.getQuantity(GoodsType.HORSES);
    	prod += additionalFoodProduction;
    	return workersCount*2 <= prod;
    }
    
	private GoodMaxProductionLocation theBestLocation(Unit worker, String ... planGoodsType) {
	    List<GoodMaxProductionLocation> productions = colony.determinePotentialMaxGoodsProduction(worker);
	    
		List<GoodMaxProductionLocation> onlyGoodsFromPlan = new ArrayList<GoodMaxProductionLocation>();
		for (GoodMaxProductionLocation p : productions) {
			if (isArrayContains(planGoodsType, p.getGoodsType().getId())) {
				//System.out.println(" - potential location - " + p.getProductionLocation() + " " + p.getGoodsType() + " " + p.getProduction());
				onlyGoodsFromPlan.add(p);
			}
		}
		
		Collections.sort(onlyGoodsFromPlan, new Comparator<GoodMaxProductionLocation>() {
			@Override
			public int compare(GoodMaxProductionLocation o1, GoodMaxProductionLocation o2) {
				return o2.getProduction() - o1.getProduction();
			}
		});
		
		if (onlyGoodsFromPlan.isEmpty()) {
			return null;
		}
		return onlyGoodsFromPlan.get(0);
	}

	private Unit workersByPriorityToPlan(
		final List<Unit> availableWorkers, 
		final String ... goodsTypeIds
	) {
	    if (goodsTypeIds.length == 0) {
	        throw new IllegalStateException("goodsTypeIds is empty");
	    }
	    if (availableWorkers.isEmpty()) {
	        throw new IllegalArgumentException("no available workers");
	    }
	    
	    Collections.sort(availableWorkers, new Comparator<Unit>() {
            @Override
            public int compare(Unit o1, Unit o2) {
                return maxProd(o2) - maxProd(o1);
            }
            
            int maxProd(Unit u) {
                int m = 0;
                for (String gtId : goodsTypeIds) {
                    int prod = (int)u.unitType.applyModifier(gtId, 10);
                    if (prod > m) {
                        m = prod;
                    }
                    // experts have higher priority for own goods production
                    if (u.unitType.getExpertProductionForGoodsId() != null 
                		&& !gtId.equals(u.unitType.getExpertProductionForGoodsId())
                		&& goodsTypeGoalIngredientsChain.contains(u.unitType.getExpertProductionForGoodsId())) {
                    	m = m / 2;
                    }
                }
                return m;
            }
        });
	    return availableWorkers.get(0);
	}

    private boolean hasGoodsToConsume(String goodsTypeId, int amount) {
    	int available = colony.productionSummary().getQuantity(goodsTypeId);
    	if (consumeWarehouseResources) {
    		available += colony.getGoodsContainer().goodsAmount(goodsTypeId);
    	}
    	return amount * 0.5 <= available;
    }
	
	private boolean hasGoodsToConsume(ProductionSummary ps) {
		for (Entry<String> entry : ps.entries()) {
			int available = colony.productionSummary().getQuantity(entry.key);
			if (consumeWarehouseResources) {
				available += colony.getGoodsContainer().goodsAmount(entry.key);
			}
			if (entry.value * 0.5f > available) {
				return false;
			}
		}
		return true;
	}
	
	private Plan nextPlan() {
	    if (plans == null || plans.length == 0) {
	        throw new IllegalStateException("no plans");
	    }
	    Plan plan = plans[nextPlanIndex];
	    nextPlanIndex++;
	    if (nextPlanIndex >= plans.length) {
	        nextPlanIndex = 0;
	    }
	    return plan;
	}

	private boolean isArrayContains(String[] array, String str) {
		for (int i=0; i<array.length; i++) {
			if (array[i].equals(str)) {
				return true;
			}
		}
		return false;
	}
	
	private GoodsType objById(String goodsTypeId) {
		return Specification.instance.goodsTypes.getById(goodsTypeId);
	}
	
	private Set<String> setOf(String ... strArray) {
		HashSet<String> set = new HashSet<String>();
		for (String st : strArray) {
			set.add(st);
		}
		return set;
	}

	private List<Unit> without(List<Unit> units, Unit excludeUnit) {
		List<Unit> ret = new ArrayList<Unit>();
		for (Unit u : units) {
			if (u.notEqualsId(excludeUnit)) {
				ret.add(u);
			}
		}
		return ret;
	}

	private Unit findById(List<Unit> units, String id) {
		for (Unit u : units) {
			if (u.equalsId(id)) {
				return u;
			}
		}
		throw new IllegalStateException("can not find entity by id: " + id);
	}

	private void createPlanProductionChain(GoodsType goodsType) {
		createPlanProductionChain(goodsType.getId());
	}
	
	private void createPlanProductionChain(Plan plan) {
		createPlanProductionChain(plan.prodGoodsIdsArray);
	}
	
	private void createPlanProductionChain(String ... goodsTypeIds) {
		goodsTypeGoalIngredientsChain.clear();
		for (String planProdGoodsTypeId : goodsTypeIds) {
			GoodsType goodsType = Specification.instance.goodsTypes.getById(planProdGoodsTypeId);
			goodsTypeGoalIngredientsChain.add(goodsType.getId());
			
			while (goodsType.getMadeFrom() != null) {
				goodsType = goodsType.getMadeFrom();
				goodsTypeGoalIngredientsChain.add(0, goodsType.getId());
			}
		}
	}

	public ColonyPlan withConsumeWarehouseResources(boolean consumeWarehouseResources) {
		this.consumeWarehouseResources = consumeWarehouseResources;
		return this;
	}
}
