package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ProductionSimulation {

	public static final UnitTypeByGoodsTypePolicy expertForGoodsType = new ExpertUnitTypeByGoodsTypePolicy();
	public static final UnitTypeByGoodsTypePolicy freeColonistsForGoodsType = new AlwaysFreeColonistForAnyGoodsTypePolicy();

	private static final Set<String> emptyExcludeLocationIds = Collections.unmodifiableSet(Collections.<String>emptySet());

	private final ColonyTileProduction simTileProduction = new ColonyTileProduction();
	private final ColonySettingProvider colonyProvider;
	private final ColonyProduction colonyProduction;
	private final ProductionValueComparator productionValueComparator;

	ProductionSimulation(ColonySettingProvider colonySettingProvider, ColonyProduction colonyProduction) {
		this.colonyProvider = colonySettingProvider;
		this.colonyProduction = colonyProduction;
		this.productionValueComparator = ProductionValueComparator.byQuantity;
	}

	public List<MaxGoodsProductionLocation> determinePotentialTerrainProductions(ColonyTile colonyTile, UnitType workerType) {
		// need for update production bonus etc
		colonyProduction.globalProductionConsumption();
		List<MaxGoodsProductionLocation> goodsProduction = new ArrayList<MaxGoodsProductionLocation>();

		List<Production> productions = colonyTile.tile.getType().productionInfo.getAttendedProductions();
		for (Production production : productions) {
			simTileProduction.init(
				colonyTile.tile,
				production,
				workerType
			);
			simTileProduction.potentialProductions(goodsProduction, colonyProvider.colonyUpdatableFeatures());
		}
		return goodsProduction;
	}

	public List<MaxGoodsProductionLocation> determinePotentialMaxGoodsProduction(UnitType workerType, boolean ignoreIndianOwner) {
		return determinePotentialMaxGoodsProduction(Specification.instance.goodsTypes.entities(), workerType, ignoreIndianOwner);
	}

	public MaxGoodsProductionLocation determinePotentialMaxTilesProduction(UnitType workerType, boolean ignoreIndianOwner) {
		MaxGoodsProductionLocation maxProd = null;

		for (GoodsType gt : Specification.instance.goodsTypes.entities()) {
			if (gt.isFarmed()) {
				MaxGoodsProductionLocation prod = maxProductionFromTile(
					gt, workerType, ignoreIndianOwner, emptyExcludeLocationIds
				);
				maxProd = MaxGoodsProductionLocation.max(maxProd, prod);
			}
		}
		return maxProd;
	}

	public List<MaxGoodsProductionLocation> determinePotentialMaxGoodsProduction(
		Collection<GoodsType> goodsTypes,
		UnitType workerType,
		boolean ignoreIndianOwner
	) {
		ProductionSummary prodCons = colonyProduction.globalProductionConsumption();
		List<MaxGoodsProductionLocation> goodsProduction = new ArrayList<MaxGoodsProductionLocation>();

		for (GoodsType gt : goodsTypes) {
			MaxGoodsProductionLocation maxProd = null;
			if (gt.isFarmed()) {
				maxProd = maxProductionFromTile(
					gt, workerType, ignoreIndianOwner, emptyExcludeLocationIds
				);
			} else {
				maxProd = maxProductionFromBuilding(
					gt, workerType, prodCons, emptyExcludeLocationIds
				);
			}
			if (maxProd != null) {
				goodsProduction.add(maxProd);
			}
		}
		return goodsProduction;
	}

	public List<MaxGoodsProductionLocation> determinePotentialMaxGoodsProduction(
		Collection<GoodsType> goodsTypes,
		UnitTypeByGoodsTypePolicy unitTypeForGoodsTypePolicy,
		boolean ignoreIndianOwner
	) {
		ProductionSummary prodCons = colonyProduction.globalProductionConsumption();
		List<MaxGoodsProductionLocation> goodsProduction = new ArrayList<MaxGoodsProductionLocation>();

		for (GoodsType gt : goodsTypes) {
			UnitType workerType = unitTypeForGoodsTypePolicy.unitType(gt);

			MaxGoodsProductionLocation maxProd = null;
			if (gt.isFarmed()) {
				maxProd = maxProductionFromTile(
					gt, workerType, ignoreIndianOwner, emptyExcludeLocationIds
				);
			} else {
				maxProd = maxProductionFromBuilding(
					gt, workerType, prodCons, emptyExcludeLocationIds
				);
			}
			if (maxProd != null) {
				goodsProduction.add(maxProd);
			}
		}
		return goodsProduction;
	}

	public MaxGoodsProductionLocation determineMaxProduction(
		GoodsType gt,
		UnitType workerType,
		boolean ignoreIndianOwner,
		Set<String> excludeLocationIds
	) {
		ProductionSummary prodCons = colonyProduction.globalProductionConsumption();

		MaxGoodsProductionLocation maxProd = null;
		if (gt.isFarmed()) {
			maxProd = maxProductionFromTile(
				gt, workerType, ignoreIndianOwner, excludeLocationIds
			);
		} else {
			maxProd = maxProductionFromBuilding(
				gt, workerType, prodCons, excludeLocationIds
			);
		}
		return maxProd;
	}

	public MaxGoodsProductionLocation determineMaxProduction(
		Collection<GoodsType> goodsTypes,
		UnitType workerType,
		boolean ignoreIndianOwner,
		Set<String> excludeLocationIds
	) {
		ProductionSummary prodCons = colonyProduction.globalProductionConsumption();
		MaxGoodsProductionLocation globalMaxProd = null;

		for (GoodsType gt : goodsTypes) {
			MaxGoodsProductionLocation maxProd = null;
			if (gt.isFarmed()) {
				maxProd = maxProductionFromTile(
					gt, workerType, ignoreIndianOwner, excludeLocationIds
				);
			} else {
				maxProd = maxProductionFromBuilding(
					gt, workerType, prodCons, excludeLocationIds
				);
			}
			globalMaxProd = MaxGoodsProductionLocation.max(globalMaxProd, maxProd);
		}
		return globalMaxProd;
	}

	public MaxGoodsProductionLocation determineMaxProduction(GoodsType goodsType, UnitType unitType, boolean ignoreIndianOwner) {
		if (goodsType.isFarmed()) {
			return maxProductionFromTile(goodsType, unitType, ignoreIndianOwner, emptyExcludeLocationIds);
		} else {
			ProductionSummary prodCons = colonyProduction.globalProductionConsumption();
			return maxProductionFromBuilding(goodsType, unitType, prodCons, emptyExcludeLocationIds);
		}
	}

    private MaxGoodsProductionLocation maxProductionFromTile(
		final GoodsType goodsType, 
		final UnitType workerType, 
		boolean ignoreIndianOwner,
		Set<String> excludeLocationIds
	) {
    	MaxGoodsProductionLocation maxProd = null;
	    
    	for (ColonyTileProduction colonyTile : colonyProvider.tiles()) {
	        if (colonyTile.hasWorker()
				|| colonyTile.tile.hasSettlement()
        		|| colonyProvider.isCenterTile(colonyTile.tile) 
        		|| colonyProvider.isTileLocked(colonyTile.tile, ignoreIndianOwner)
				|| excludeLocationIds.contains(colonyTile.getId())
    		) {
	            continue;
	        }
	        simTileProduction.init(
				colonyTile.tile,
				colonyTile.tile.getType().productionInfo.firstAttendentProduction(goodsType),
				workerType
			);
			maxProd = simTileProduction.maxGoodsProduction(
				goodsType, maxProd,
				colonyProvider.colonyUpdatableFeatures(),
				productionValueComparator
			);
	    }
	    return maxProd;
	}

	/**
	 * Return increase in production. Not total production.
	 */
	private MaxGoodsProductionLocation maxProductionFromBuilding(
		GoodsType goodsType, UnitType workerType, 
		ProductionSummary prodCons,
		Set<String> excludeLocationIds
	) {
		MaxGoodsProductionLocation maxProd = null;
		
		for (BuildingProduction productionBuilding : colonyProvider.buildings()) {
			if (!productionBuilding.canAddWorker(workerType) || excludeLocationIds.contains(productionBuilding.getId())) {
				continue;
			}
			int quantity = productionBuilding.singleWorkerProduction(
				workerType, goodsType, prodCons,
				colonyProvider.warehouse(),
				colonyProvider.colonyUpdatableFeatures()
			);
			
			if (quantity > 0 && productionValueComparator.more(maxProd, goodsType, quantity)) {
				if (maxProd == null) {
					maxProd = new MaxGoodsProductionLocation();
				}
				maxProd.goodsType = goodsType;
				maxProd.production = quantity;
				maxProd.buildingType = productionBuilding.buildingType;
			}
		}
		return maxProd;
	}

	public void determineMaxPotentialProduction(
		BuildingType buildingType, GoodsType goodsType, UnitType workerType,
		GoodsCollection prod, GoodsCollection cons
	) {
		BuildingProduction buildingProduction = colonyProvider.buildings().getByIdOrNull(buildingType.getId());
		if (buildingProduction == null || !buildingProduction.canAddWorker(workerType)) {
			return;
		}
		// init buildings etc
		colonyProduction.globalProductionConsumption();
		buildingProduction.determineMaxPotentialProduction(colonyProvider.colonyUpdatableFeatures(), workerType, prod, cons, goodsType.getId());
	}

	public void determineMaxPotentialProduction(GoodsType goodsType, UnitType workerType, GoodsCollection prod, GoodsCollection cons) {
		if (!workerType.isPerson()) {
			throw new IllegalArgumentException("worker[" + workerType + "] is not a person ");
		}
		// init buildings etc
		colonyProduction.globalProductionConsumption();

		for (BuildingProduction buildingProduction : colonyProvider.buildings()) {
			if (!buildingProduction.canAddWorker(workerType)) {
				continue;
			}
			buildingProduction.determineMaxPotentialProduction(colonyProvider.colonyUpdatableFeatures(), workerType, prod, cons, goodsType.getId());
		}
	}

	public static interface UnitTypeByGoodsTypePolicy {
		UnitType unitType(GoodsType goodsType);
	}

	static class ExpertUnitTypeByGoodsTypePolicy implements UnitTypeByGoodsTypePolicy {

		@Override
		public UnitType unitType(GoodsType goodsType) {
			return Specification.instance.expertUnitTypeForGoodsType(goodsType);
		}
	}

	static class AlwaysFreeColonistForAnyGoodsTypePolicy implements UnitTypeByGoodsTypePolicy {

		@Override
		public UnitType unitType(GoodsType goodsType) {
			return Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
		}
	}

}
