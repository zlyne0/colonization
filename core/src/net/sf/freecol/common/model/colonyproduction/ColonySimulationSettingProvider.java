package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyLiberty;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;

import java.util.HashMap;
import java.util.List;

public class ColonySimulationSettingProvider implements ColonySettingProvider {
    private final DefaultColonySettingProvider defaultColonySettingProvider;

    private final ColonyLiberty colonyLiberty = new ColonyLiberty();
    private Modifier productionBonus = new Modifier(Modifier.COLONY_PRODUCTION_BONUS, Modifier.ModifierType.ADDITIVE, 0);
    private boolean consumeWarehouseResources = false;
    private java.util.Map<GoodsType, BuildingType> buildingTypeByAttendedOutputGoods;

    public ColonySimulationSettingProvider(Colony colony) {
        defaultColonySettingProvider = new DefaultColonySettingProvider(colony);
        colonyLiberty.copy(colony.colonyLiberty);

        defaultColonySettingProvider.initProductionLocations();
    }

    public void resetProductionLocations() {
        defaultColonySettingProvider.initProductionLocations();
    }

    @Override
    public void initProductionLocations() {
        if (!this.consumeWarehouseResources) {
            defaultColonySettingProvider.warehouse().reset(defaultColonySettingProvider.colony.warehouseCapacity());
        }

        colonyLiberty.updateSonOfLiberty(
            defaultColonySettingProvider.colony.getOwner(),
            defaultColonySettingProvider.workers().size()
        );
        productionBonus.setValue(colonyLiberty.productionBonus());
    }

    @Override
    public MapIdEntities<ColonyTileProduction> tiles() {
        return defaultColonySettingProvider.tiles();
    }

    @Override
    public MapIdEntities<BuildingProduction> buildings() {
        return defaultColonySettingProvider.buildings();
    }

    @Override
    public List<Worker> workers() {
        return defaultColonySettingProvider.workers();
    }

    @Override
    public Modifier productionBonus() {
        return productionBonus;
    }

    @Override
    public ObjectWithFeatures colonyUpdatableFeatures() {
        return defaultColonySettingProvider.colonyUpdatableFeatures();
    }

    @Override
    public Warehouse warehouse() {
        return defaultColonySettingProvider.warehouse();
    }

    @Override
    public boolean isCenterTile(Tile tile) {
        return defaultColonySettingProvider.isCenterTile(tile);
    }

    @Override
    public boolean isTileLocked(Tile tile, boolean ignoreIndianOwner) {
        return defaultColonySettingProvider.isTileLocked(tile, ignoreIndianOwner);
    }

    public void addWorkerToColony(UnitType workerType, MaxGoodsProductionLocation maxProd) {
        if (maxProd.colonyTile != null) {
            defaultColonySettingProvider.addWorker(maxProd.colonyTile, workerType, maxProd.tileTypeInitProduction);
        }
        if (maxProd.buildingType != null) {
            defaultColonySettingProvider.addWorker(maxProd.buildingType, workerType);
        }
    }

    public void addWorker(Tile tile, Unit worker, Production production) {
        defaultColonySettingProvider.addWorker(tile, worker, production);
    }

    public void addWorker(BuildingType buildingType, Unit worker) {
        defaultColonySettingProvider.addWorker(buildingType, worker);
    }

    public void addWorker(BuildingType buildingType, UnitType workerType) {
        defaultColonySettingProvider.addWorker(buildingType, workerType);
    }

    public ColonySimulationSettingProvider withConsumeWarehouseResources() {
        this.consumeWarehouseResources = true;
        return this;
    }

    public void clearWorkersAllocation() {
        defaultColonySettingProvider.clearWorkersAllocation();
    }

    public void putWorkersToColonyViaAllocation() {
        //defaultColonySettingProvider.printAllocation();
        defaultColonySettingProvider.putWorkersToColonyViaAllocation();
    }

    /**
     * find building type that produce {@link GoodsType} and has free slot for worker
     * @return null when no building or can not add worker
     */
    BuildingType findBuildingType(GoodsType goodsType, UnitType worker) {
        BuildingType buildingType = findBuildingTypeByAttendedOutputGoods(goodsType);
        if (buildingType == null) {
            return null;
        }
        BuildingProduction colonyBuildingProduction = defaultColonySettingProvider.buildings().getByIdOrNull(buildingType);
        if (colonyBuildingProduction == null) {
            return null;
        }
        if (colonyBuildingProduction.canAddWorker(worker)) {
            return buildingType;
        }
        return null;
    }

    public BuildingType findBuildingTypeByAttendedOutputGoods(GoodsType goodsType) {
        // in colony scope. Buildings in colony
        if (buildingTypeByAttendedOutputGoods != null) {
            return buildingTypeByAttendedOutputGoods.get(goodsType);
        }
        initBuildingTypeByAttendedOutputGoods();
        return buildingTypeByAttendedOutputGoods.get(goodsType);
    }

    private void initBuildingTypeByAttendedOutputGoods() {
        buildingTypeByAttendedOutputGoods = new HashMap<GoodsType, BuildingType>();
        for (GoodsType goodsType : Specification.instance.goodsTypes) {
            if (goodsType.isFarmed()) {
                continue;
            }
            for (BuildingProduction buildingProduction : defaultColonySettingProvider.buildings()) {
                if (buildingProduction.buildingType.hasAttendedOutputGoods(goodsType)) {
                    buildingTypeByAttendedOutputGoods.put(goodsType, buildingProduction.buildingType);
                }
            }
        }
    }

    public void addBuilding(BuildingType buildingType) {
        defaultColonySettingProvider.addBuilding(buildingType);
        if (buildingTypeByAttendedOutputGoods != null) {
            buildingTypeByAttendedOutputGoods.clear();
        }
    }
}
