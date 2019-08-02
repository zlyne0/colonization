package net.sf.freecol.common.model.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.UnitConsumption;
import net.sf.freecol.common.model.specification.GoodsType;

public class ColonyPlan {

	enum Plan {
		// only food
		Food(GoodsType.GRAIN, GoodsType.FISH),
		Bell(GoodsType.BELLS),
		Building(),
		Zasoby(),
		ZasobyLuksusowe(),
		Tools(),
		Muskiet(); 
		
		private final Set<String> prodGoodsId = new HashSet<String>();
		
		private Plan(String ... prodGoodsId) {
			for (String id : prodGoodsId) {
				this.prodGoodsId.add(id);
			}
		}
		
		public boolean contains(String goodId) {
			return goodId != null && prodGoodsId.contains(goodId);
		}
	}
	
	private final Colony colony;
	
	public ColonyPlan(Colony colony) {
		this.colony = colony;
	}
	
	public void execute() {
		execute(Plan.Bell);
	}
	
	public void execute(Plan plan) {
		List<Unit> availableWorkers = new ArrayList<Unit>(colony.settlementWorkers().size());
		
		for (Unit unit : colony.settlementWorkers()) {
			unit.changeUnitLocation(colony.tile);
			unit.canChangeState(UnitState.SKIPPED);
			availableWorkers.add(unit);
		}
		colony.updateModelOnWorkerAllocationOrGoodsTransfer();
		colony.updateColonyPopulation();
		
		while (!availableWorkers.isEmpty()) {
    		Unit theBestWorkerForPlan = workersByPriorityToPlan(plan.prodGoodsId, availableWorkers);
    		GoodMaxProductionLocation theBestLocation = theBestLocation(plan, theBestWorkerForPlan);
    		if (theBestLocation == null) {
    		    System.out.println("no more good location");
    		    break;
    		}
    		if (canSustainNewWorker(theBestWorkerForPlan, theBestLocation)) {
    		    addWorkerToProductionLocation(theBestWorkerForPlan, theBestLocation);
    		    availableWorkers.remove(theBestWorkerForPlan);
    		} else {
    		    if (!findWorkerForFood(availableWorkers)) {
    		        System.out.println("can not produce more food");
    		        break;
    		    }
    		}
		}
	}

	private boolean findWorkerForFood(List<Unit> availableWorkers) {
        Unit foodWorker = workersByPriorityToPlan(Plan.Food.prodGoodsId, availableWorkers);
        GoodMaxProductionLocation foodLocation = theBestLocation(Plan.Food, foodWorker);
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
			// when unit produce what consume, unit can sustain yourself
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

	private GoodMaxProductionLocation theBestLocation(Plan plan, Unit worker) {
	    List<GoodMaxProductionLocation> productions = colony.determinePotentialMaxGoodsProduction(worker);
	    
		List<GoodMaxProductionLocation> onlyGoodsFromPlan = new ArrayList<GoodMaxProductionLocation>();
		for (GoodMaxProductionLocation p : productions) {
			if (plan.contains(p.getGoodsType().getId())) {
				System.out.println(" - potential location - " + p.getProductionLocation() + " " + p.getGoodsType() + " " + p.getProduction());
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
	
	private Unit workersByPriorityToPlan(final Set<String> goodsTypes, List<Unit> availableWorkers) {
	    if (goodsTypes.isEmpty()) {
	        throw new IllegalStateException("is empty");
	    }
	    if (availableWorkers.isEmpty()) {
	        throw new IllegalArgumentException("no available workers");
	    }
	    
	    Collections.sort(availableWorkers, new Comparator<Unit>() {
            @Override
            public int compare(Unit o1, Unit o2) {
                return maxProd(o2, goodsTypes) - maxProd(o1, goodsTypes);
            }
            
            int maxProd(Unit u, Set<String> gts) {
                int m = 0;
                for (String gtId : gts) {
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
}
