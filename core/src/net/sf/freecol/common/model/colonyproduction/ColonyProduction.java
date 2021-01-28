package net.sf.freecol.common.model.colonyproduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.ProductionConsumption;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitConsumption;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;

class ColonyProduction {

	private final List<ColonyTileProduction> tiles = new ArrayList<ColonyTileProduction>(9);
	private final List<BuildingProduction> buildings;
	private final Warehouse warehouse;
	private final List<Worker> workers = new ArrayList<Worker>();
	
	private final java.util.Map<String,ProductionConsumption> prodConsByProducer = new HashMap<String, ProductionConsumption>();	
	private final ProductionSummary globalProductionConsumption = new ProductionSummary();
	
	private Modifier colonyProductionBonus;
	private ObjectWithFeatures colonyFeatures;
	private boolean updateRequired = true;
	
	public ColonyProduction(Colony colony) {
		buildings = new ArrayList<BuildingProduction>(colony.buildings.size());
		
		colonyProductionBonus = colony.productionBonus();
		colonyFeatures = colony.colonyUpdatableFeatures;
		
		warehouse = new Warehouse(colony);
		
		for (ColonyTile colonyTile : colony.colonyTiles) {
			ColonyTileProduction tileProd = new ColonyTileProduction();
			tileProd.init(colonyTile.tile, colonyTile.tileProduction(), colonyTile.getWorker(), workers);
			tiles.add(tileProd);
		}
		
		for (Building building : colony.buildings.sortedEntities()) {
			BuildingProduction buildingProduction = new BuildingProduction();
			buildingProduction.init(building, building.buildingType, building.getUnits(), workers);
			buildings.add(buildingProduction);
		}
	}

	public void updateRequest() {
		this.updateRequired = true;
	}
	
	public ProductionSummary globalProductionConsumption() {
		update();
		return globalProductionConsumption;
	}
	
	private void update() {
		if (!updateRequired) {
			return;
		}
		globalProductionConsumption.makeEmpty();
		
		initBellsProduction();
		tilesProduction();
		workersFoodConsumption();
		buildingsProduction();
		consolidateFoods();
		
		updateRequired = false;
	}

	private void initBellsProduction() {
        int unitsThatUseNoBells = Specification.options.getIntValue(GameOptions.UNITS_THAT_USE_NO_BELLS);
        int amount = Math.min(unitsThatUseNoBells, workers.size());
        globalProductionConsumption.addGoods(GoodsType.BELLS, amount);
	}

	private void tilesProduction() {
		for (ColonyTileProduction colonyTileProduction : tiles) {
			ProductionConsumption ps = colonyTileProduction.productionSummaryForTile(colonyFeatures);
			prodConsByProducer.put(colonyTileProduction.tile.getId(), ps);
			globalProductionConsumption.addGoods(ps.realProduction);
		}
	}

	private void workersFoodConsumption() {
		for (Worker worker : workers) {
			for (UnitConsumption uc : worker.unitType.unitConsumption) {
        		if (uc.getId().equals(GoodsType.FOOD)) {
        			if (globalProductionConsumption.decreaseIfHas(GoodsType.FISH, uc.getQuantity())) {
        				// can decrease
        			} else {
        				// can not decrease
        				globalProductionConsumption.decrease(GoodsType.GRAIN, uc.getQuantity());
        			}
        		} else {
	        		globalProductionConsumption.decrease(uc.getId(), uc.getQuantity());
        		}
			}
		}
	}

	private void buildingsProduction() {
		for (BuildingProduction bp : buildings) {
			ProductionConsumption pc = bp.determineProductionConsumption(warehouse, globalProductionConsumption, colonyProductionBonus.asInt());
            pc.baseProduction.applyModifiers(colonyFeatures);
            pc.realProduction.applyModifiers(colonyFeatures);
			
			prodConsByProducer.put(bp.building.getId(), pc);
        	
        	globalProductionConsumption.addGoods(pc.realConsumption);
        	globalProductionConsumption.addGoods(pc.realProduction);
		}
	}

    private void consolidateFoods() {
        for (Entry<String> entry : globalProductionConsumption.entries()) {
            if (GoodsType.isFoodGoodsType(entry.key)) {
                int q = entry.value;
                globalProductionConsumption.decrease(entry.key, q);
                globalProductionConsumption.addGoods(GoodsType.FOOD, q);
            }
        }
    }
}
