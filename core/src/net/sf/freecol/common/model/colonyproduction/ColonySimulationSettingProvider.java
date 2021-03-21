package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyLiberty;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.Modifier;

import java.util.List;

public class ColonySimulationSettingProvider implements ColonySettingProvider {
    private final DefaultColonySettingProvider defaultColonySettingProvider;

    private final ColonyLiberty colonyLiberty = new ColonyLiberty();
    private Modifier productionBonus = new Modifier(Modifier.COLONY_PRODUCTION_BONUS, Modifier.ModifierType.ADDITIVE, 0);
    private boolean consumeWarehouseResources = false;

    public ColonySimulationSettingProvider(Colony colony) {
        defaultColonySettingProvider = new DefaultColonySettingProvider(colony);
        colonyLiberty.copy(colony.colonyLiberty);

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

    public void addWorker(Unit worker, MaxGoodsProductionLocation maxProd) {
        if (maxProd.colonyTile != null) {
            defaultColonySettingProvider.addWorker(maxProd.colonyTile, worker, maxProd.tileTypeInitProduction);
        }
        if (maxProd.buildingType != null) {
            defaultColonySettingProvider.addWorker(maxProd.buildingType, worker);
        }
    }

    public void addWorkerToColony(UnitType workerType, BuildingType buildingType) {
        defaultColonySettingProvider.addWorker(buildingType, workerType);
    }

    public void withConsumeWarehouseResources() {
        this.consumeWarehouseResources = true;
    }

    public void clearAllProductionLocations() {
        defaultColonySettingProvider.clearAllProductionLocations();
    }

    public void putWorkersToColonyViaAllocation() {
        defaultColonySettingProvider.putWorkersToColonyViaAllocation();
    }
}
