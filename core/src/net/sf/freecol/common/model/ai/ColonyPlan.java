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
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.Production;
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
	
	enum Plan {
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
			for (String id : prodGoodsId) {
				this.prodGoodsId.add(id);
			}
		}
		
		public boolean contains(String goodId) {
			return goodId != null && prodGoodsId.contains(goodId);
		}
	}
	
    private final ProductionSummary prod = new ProductionSummary(); 
    private final ProductionSummary ingredients = new ProductionSummary();
    
	private boolean consumeWarehouseResources = false;
	
	private final Colony colony;
    private Plan[] plans;
    private int nextPlanIndex = 0;
    
	public ColonyPlan(Colony colony) {
		this.colony = colony;
	}
	
	public void execute() {
		execute2(ColonyPlan.Plan.Muskets);
	}
	
	class GoodMaxProductionLocationWithUnit {
		int score = -1;
		Unit unit;
		GoodMaxProductionLocation production;
		
		GoodMaxProductionLocationWithUnit() {
		}

		public void update(int score, Unit unit, GoodMaxProductionLocation prod) {
			if (score > this.score) {
				this.score = score;
				this.unit = unit;
				this.production = prod;
			}
		}
		
		public String toString() {
			return "goodsType: " + production.getGoodsType() + ", score: " + score + ", unit: " + unit.toStringTypeLocation();
		}
		
	}
	
	public void mostValueable() {
        List<Unit> availableWorkers = new ArrayList<Unit>(colony.settlementWorkers().size());
        removeWorkersFromColony(availableWorkers);

        //List<GoodMaxProductionLocationWithUnit> unitMaxProd = new ArrayList<ColonyPlan.GoodMaxProductionLocationWithUnit>();
        /*
        {
        	GoodMaxProductionLocationWithUnit max = new GoodMaxProductionLocationWithUnit();
	        for (Unit u : availableWorkers) {
	        	//System.out.println("for worker " + u);
	        	List<GoodMaxProductionLocation> potentialProduction = colony.determinePotentialMaxGoodsProduction(u);
	        	for (GoodMaxProductionLocation pgp : potentialProduction) {
	        		if (pgp.getGoodsType().isStorable() && !colony.getOwner().market().hasArrears(pgp.getGoodsType())) {
	        			int score = 0;
	    				score = colony.getOwner().market().getSalePrice(pgp.getGoodsType(), pgp.getProduction());
	    				max.update(score, u, pgp);
	        		}
				}
	        }
	    	System.out.println("max " + max);
        }
*/    	
    	{
        	GoodMaxProductionLocationWithUnit max = new GoodMaxProductionLocationWithUnit();
    		List<GoodMaxProductionLocation> potentialProduction = new ArrayList<GoodMaxProductionLocation>();
	    	for (Unit u : availableWorkers) {
	    		potentialProduction.clear();
	    		colony.determinePotentialColonyTilesProduction(u, potentialProduction);
	    		
	    		for (GoodMaxProductionLocation pgp : potentialProduction) {
	    			//System.out.println("" + pgp.getGoodsType() + " " + pgp.getProduction() + " " + pgp.getGoodsType().getMakes());
	    			
	    			if (pgp.getGoodsType().isStorable() && !colony.getOwner().market().hasArrears(pgp.getGoodsType())) {
	        			int score = 0;
	    				score = colony.getOwner().market().getSalePrice(pgp.getGoodsType(), pgp.getProduction());
	    				max.update(score, u, pgp);
	    			}
	    		}
	    		
	    		//break;
	    	}
	    	System.out.println("max " + max);
    	}
    
    	{
    		for (Unit worker : availableWorkers) {
	    		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
	    			if (!isLuxery(goodsType)) {
	    				continue;
	    			}
	    			System.out.println("goodsType " + goodsType + " " + goodsType.isRefined() + " " + goodsType.getMadeFrom());
	    			Building building = findBuildingByGoodsType(goodsType);
	    			if (building == null || !building.canAddWorker(worker)) {
	    				continue;
	    			}
	    			
	    			prod.makeEmpty();
	    			ingredients.makeEmpty();
	    			building.determineMaxPotentialProduction(colony, worker, prod, ingredients, goodsType.getId());
	    			System.out.println(" worker " + worker.unitType + " " + prod + " " + ingredients);
				}
	    		break;
    		}
    	}

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
        	if (canSustainNewWorker(worker, location)) {
        		addWorkerToProductionLocation(worker, location);
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
	
    private void addWorkerToProductionLocation(Unit unit, GoodMaxProductionLocation theBestLocation) {
        if (theBestLocation.getColonyTile() != null) {
        	colony.addWorkerToTerrain(theBestLocation.getColonyTile(), unit, theBestLocation.getGoodsType());
        }
        if (theBestLocation.getBuilding() != null) {
        	colony.addWorkerToBuilding(theBestLocation.getBuilding(), unit);
        }
        colony.updateModelOnWorkerAllocationOrGoodsTransfer();
        colony.updateColonyPopulation();
    }

	private boolean canSustainNewWorker(Unit unit, GoodMaxProductionLocation prodLocation) {
		ProductionSummary productionSummary = colony.productionSummary();
		for (UnitConsumption unitConsumption : unit.unitType.unitConsumption.entities()) {
			if (unitConsumption.getTypeId().equals(GoodsType.BELLS)) {
				// do not care
				continue;
			}
			int prod = productionSummary.getQuantity(unitConsumption.getTypeId());
			// when unit produce what consume, unit can sustain himself
			if (GoodsType.isFoodGoodsType(unitConsumption.getTypeId()) && prodLocation.getGoodsType().isFood()) {
		        prod += prodLocation.getProduction();
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
	
	private Unit workersByPriorityToPlan(List<Unit> availableWorkers, final String ... goodsTypeIds) {
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
                }
                return m;
            }
        });
	    return availableWorkers.get(0);
	}

    private boolean hasGoodsToConsume(String goodsTypeId, int amount) {
        if (consumeWarehouseResources) {
            return colony.getGoodsContainer().hasPart(goodsTypeId, amount, 0.5f);
        } else {
            return colony.productionSummary().hasPart(goodsTypeId, amount, 0.5f);
        }
    }
	
	private boolean hasGoodsToConsume(ProductionSummary ps) {
		if (consumeWarehouseResources) {
			return colony.getGoodsContainer().hasPart(ps, 0.5f);
		} else {
			return colony.productionSummary().hasPart(ps, 0.5f);
		}
	}
	
	public Plan nextPlan() {
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

	public ColonyPlan withConsumeWarehouseResources(boolean consumeWarehouseResources) {
		this.consumeWarehouseResources = consumeWarehouseResources;
		return this;
	}
}
