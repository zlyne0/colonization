package net.sf.freecol.common.model.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.MapIdEntitiesReadOnly;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.GoodsType;

public class ColonyPlan {

	enum Plan {
		// only food
		Food("model.goods.grain", "model.goods.fish"),
		Bell(),
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
		execute(Plan.Food);
	}
	
	public void execute(Plan plan) {
		MapIdEntities<Unit> allWorkers = new MapIdEntities<Unit>();
		for (Unit unit : colony.settlementWorkers()) {
			unit.changeUnitLocation(colony.tile);
			allWorkers.add(unit);
		}
		MapIdEntities<Unit> workersByPriorityToPlan = workersByPriorityToPlan(plan, allWorkers);
		
		System.out.println("workersByPriorityToPlan");
		for (Unit unit : workersByPriorityToPlan.entities()) {
			System.out.println("  worker: " + unit.getId() + " " + unit.unitType.getId());
			List<GoodMaxProductionLocation> productions = colony.determinePotentialMaxGoodsProduction(unit);
			
			GoodMaxProductionLocation theBestLocation = theBestLocation(plan, productions);
			if (theBestLocation != null) {
				if (theBestLocation.getColonyTile() != null) {
				    colony.addWorkerToTerrain(theBestLocation.getColonyTile(), unit, theBestLocation.getGoodsType());
				}
				System.out.println("    the best location " + theBestLocation);
			} else {
				System.out.println("not good location");
			}
		}
	}

	private GoodMaxProductionLocation theBestLocation(Plan plan, List<GoodMaxProductionLocation> productions) {
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
	
	private MapIdEntities<Unit> workersByPriorityToPlan(Plan plan, MapIdEntitiesReadOnly<Unit> allWorkers) {
		MapIdEntities<Unit> workers = MapIdEntities.linkedMapIdEntities();

		// first on list experts
		for (Unit w : allWorkers.entities()) {
			if (plan.contains(w.unitType.expertProductionForGoodsId)) {
				workers.add(w);
			}
		}
		for (Unit w : allWorkers.entities()) {
			if (!workers.containsId(w)) {
				workers.add(w);
			}
		}
		return workers;
	}
}
