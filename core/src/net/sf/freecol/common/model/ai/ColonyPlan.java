package net.sf.freecol.common.model.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
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
		Zasoby(),
		ZasobyLuksusowe(),
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
		execute2(ColonyPlan.Plan.Tools);
	}
	
	public void execute(Plan ... plans) {
	    this.plans = plans;
	    
        List<Unit> availableWorkers = new ArrayList<Unit>(colony.settlementWorkers().size());
        removeWorkersFromColony(availableWorkers);
	    
        List<Plan> noWork = new ArrayList<ColonyPlan.Plan>();
        
        while (!availableWorkers.isEmpty()) {
            Plan plan = nextPlan();

            Unit theBestWorkerForPlan = workersByPriorityToPlan(availableWorkers, plan.prodGoodsIdsArray);
            AssignStatus assignStatus = assignWorkerToProduction(theBestWorkerForPlan, availableWorkers, plan.prodGoodsIdsArray);
            
            if (assignStatus == AssignStatus.OK) {
                noWork.clear();
            } else {
                noWork.add(plan);
            }
            // when no location and worker for any plan
            if (noWork.size() == this.plans.length) {
                break;
            }
        }
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

        while (!availableWorkers.isEmpty()) {
        	if (stos.isEmpty()) {
                Plan plan = nextPlan();
                stos.add(plan.prodGoodsIdsArray);
        	}
        	
        	String[] goodsTypeToProduce = stos.pop();
        	Unit worker = workersByPriorityToPlan(availableWorkers, goodsTypeToProduce);

        	if (lackOfIngedients(goodsTypeToProduce, worker, stos)) {
        	    goodsTypeToProduce = stos.pop();
        	    worker = workersByPriorityToPlan(availableWorkers, goodsTypeToProduce);
        	}
        	
        	GoodMaxProductionLocation location = theBestLocation(worker, goodsTypeToProduce);
        	if (location == null) {
        		// no location, try next plan
        		return;
        	}
        	if (canSustainNewWorker(worker, location)) {
        		addWorkerToProductionLocation(worker, location);
        		availableWorkers.remove(worker);
        	} else {
        		stos.addFirst(Plan.Food.prodGoodsIdsArray);
        	}
        }
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
	
	public void executeBuildingPlan() {
	    Plan buildingPlan = Plan.Building;
	    
        List<Unit> availableWorkers = new ArrayList<Unit>(colony.settlementWorkers().size());
        removeWorkersFromColony(availableWorkers);
		
        while (!availableWorkers.isEmpty()) {
            for (String goodsTypeId : buildingPlan.prodGoodsIdsArray) {
                if (tryProduceGoodsType(goodsTypeId, availableWorkers) != AssignStatus.OK) {
                    return;
                }
            }
        }
	}
	
	private AssignStatus tryProduceGoodsType(String hammer, List<Unit> availableWorkers) {
        Unit hammerWorker = workersByPriorityToPlan(availableWorkers, hammer);
        ProductionSummary prod = new ProductionSummary(); 
        ProductionSummary ingredients = new ProductionSummary();
        
        colony.determineMaxPotentialProduction(hammer, hammerWorker, prod, ingredients);
        
        if (ingredients.isEmpty()) {
            AssignStatus assignStatus = assignWorkerToProduction(hammerWorker, availableWorkers, hammer);
            if (assignStatus != AssignStatus.OK) {
                return assignStatus;
            }
        } else {
            for (Entry<String> ingredient : ingredients.entries()) {
                if (!hasGoodsToConsume(ingredient.key, ingredient.value)) {
                    Unit ingredientWorker = workersByPriorityToPlan(availableWorkers, ingredient.key);
                    
                    AssignStatus assignStatus = assignWorkerToProduction(ingredientWorker, availableWorkers, ingredient.key);
                    if (assignStatus != AssignStatus.OK) {
                        return assignStatus;
                    }
                    // try assign to produced goods
                } else {
                    AssignStatus assignStatus = assignWorkerToProduction(hammerWorker, availableWorkers, hammer);
                    if (assignStatus != AssignStatus.OK) {
                        return assignStatus;
                    }
                }
            }
        }
        return AssignStatus.OK;
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

	private AssignStatus assignWorkerToProduction(Unit worker, List<Unit> availableWorkers, String ... goodsTypeIds) {
    	GoodMaxProductionLocation location = theBestLocation(worker, goodsTypeIds);
    	if (location == null) {
    		return AssignStatus.NO_LOCATION;
    	}
    	if (canSustainNewWorker(worker, location)) {
            addWorkerToProductionLocation(worker, location);
            availableWorkers.remove(worker);
    	} else {
            if (!findWorkerForFood(availableWorkers)) {
            	return AssignStatus.NO_FOOD;
            }
    	}
    	return AssignStatus.OK;
	}

	private boolean findWorkerForFood(List<Unit> availableWorkers) {
        Unit foodWorker = workersByPriorityToPlan(availableWorkers, Plan.Food.prodGoodsIdsArray);
        GoodMaxProductionLocation foodLocation = theBestLocation(foodWorker, Plan.Food.prodGoodsIdsArray);
        if (foodLocation == null) {
            return false;
        }
        if (canSustainNewWorker(foodWorker, foodLocation)) {
            addWorkerToProductionLocation(foodWorker, foodLocation);
            availableWorkers.remove(foodWorker);
            return true;
        }
        return false;
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
