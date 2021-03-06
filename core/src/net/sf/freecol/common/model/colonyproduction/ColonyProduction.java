package net.sf.freecol.common.model.colonyproduction;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.ProductionConsumption;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitConsumption;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ColonyProduction {

	private final Warehouse warehouse = new Warehouse();

	private final java.util.Map<String, ProductionConsumption> prodConsByProducer = new HashMap<String, ProductionConsumption>();	
	private final ProductionSummary globalProductionConsumption = new ProductionSummary();
	
	private Modifier colonyProductionBonus;
	private ObjectWithFeatures colonyFeatures;
	private boolean updateRequired = true;
	
	private final ColonySettingProvider colonyProvider;
	
	public ColonyProduction(ColonySettingProvider colonyProvider) {
		this.colonyProvider = colonyProvider;
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
		colonyProvider.initWarehouse(warehouse);
		colonyProvider.initProductionLocations();
		colonyProductionBonus = colonyProvider.productionBonus();
		colonyFeatures = colonyProvider.colonyUpdatableFeatures();
		
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
        int amount = Math.min(unitsThatUseNoBells, colonyProvider.workers().size());
        globalProductionConsumption.addGoods(GoodsType.BELLS, amount);
	}

	private void tilesProduction() {
		for (ColonyTileProduction colonyTileProduction : colonyProvider.tiles()) {
			ProductionConsumption ps = colonyTileProduction.productionSummaryForTile(colonyFeatures);
			prodConsByProducer.put(colonyTileProduction.tile.getId(), ps);
			globalProductionConsumption.addGoods(ps.realProduction);
		}
	}

	private void workersFoodConsumption() {
		for (Worker worker : colonyProvider.workers()) {
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
		for (BuildingProduction bp : colonyProvider.buildings()) {
			ProductionConsumption pc = bp.determineProductionConsumption(warehouse, globalProductionConsumption, colonyProductionBonus.asInt());
            pc.baseProduction.applyModifiers(colonyFeatures);
            pc.realProduction.applyModifiers(colonyFeatures);
			
			prodConsByProducer.put(bp.buildingType.getId(), pc);
        	
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
    
	public List<MaxGoodsProductionLocation> determinePotentialMaxGoodsProduction(
		Collection<GoodsType> goodsTypes, 
		UnitType workerType, 
		boolean ignoreIndianOwner
	) {
    	ProductionSimulation productionSimulation = new ProductionSimulation(colonyProvider);

        ProductionSummary prodCons = globalProductionConsumption();
		List<MaxGoodsProductionLocation> goodsProduction = new ArrayList<MaxGoodsProductionLocation>();

        for (GoodsType gt : goodsTypes) {
        	MaxGoodsProductionLocation maxProd = null;
            if (gt.isFarmed()) {
                maxProd = productionSimulation.maxProductionFromTile(
            		gt, workerType, 
            		ignoreIndianOwner
        		);
            } else {
                maxProd = productionSimulation.maxProductionFromBuilding(
            		gt, workerType, 
            		prodCons, warehouse
        		);
            }
            if (maxProd != null) {
                goodsProduction.add(maxProd);
            }
        }
        return goodsProduction;
    }

    public boolean canSustainNewWorker(UnitType workerType) {
    	return canSustainWorkers(1, 0);
    }
	
    public boolean canSustainWorkers(int workersCount, int additionalFoodProduction) {
    	ProductionSummary productionSummary = globalProductionConsumption();    	
    	int prod = productionSummary.getQuantity(GoodsType.FOOD);
    	prod += productionSummary.getQuantity(GoodsType.HORSES);
    	prod += additionalFoodProduction;
    	return workersCount*2 <= prod;
    }
}
