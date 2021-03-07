package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyLiberty;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.util.MapList;

import java.util.List;
import java.util.Map;

public class ColonySimulationSettingProvider implements ColonySettingProvider {
    private final DefaultColonySettingProvider defaultColonySettingProvider;

    private final ColonyLiberty colonyLiberty = new ColonyLiberty();
    private Modifier productionBonus = new Modifier(Modifier.COLONY_PRODUCTION_BONUS, Modifier.ModifierType.ADDITIVE, 0);
    private boolean consumeWarehouseResources = false;

    private final MapIdEntities<ColonyTileProduction> additionalTileWorkers = new MapIdEntities<ColonyTileProduction>();;
    private final MapList<String, UnitType> additionalBuildingWorkers = new MapList<String, UnitType>();

    public ColonySimulationSettingProvider(Colony colony) {
        defaultColonySettingProvider = new DefaultColonySettingProvider(colony);
        colonyLiberty.copy(colony.colonyLiberty);
    }

    @Override
    public void initProductionLocations() {
        defaultColonySettingProvider.initProductionLocations();

        if (!this.consumeWarehouseResources) {
            defaultColonySettingProvider.warehouse().reset(defaultColonySettingProvider.colony.warehouseCapacity());
        }

        for (ColonyTileProduction atw : additionalTileWorkers) {
            defaultColonySettingProvider.addWorker(atw);
        }
        for (Map.Entry<String, List<UnitType>> addBuildingWorkers : additionalBuildingWorkers.entrySet()) {
            BuildingType buildingType = Specification.instance.buildingTypes.getById(addBuildingWorkers.getKey());
            defaultColonySettingProvider.addWorker(addBuildingWorkers.getValue(), buildingType);
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
            ColonyTileProduction tileProduction = new ColonyTileProduction(maxProd.colonyTile);
            tileProduction.init(maxProd.tileTypeInitProduction, workerType);
            additionalTileWorkers.add(tileProduction);
        }
        if (maxProd.buildingType != null) {
            additionalBuildingWorkers.add(maxProd.buildingType.getId(), workerType);
        }
    }

    public void addWorkerToColony(UnitType workerType, BuildingType buildingType) {
        additionalBuildingWorkers.add(buildingType.getId(), workerType);
    }

    public void withConsumeWarehouseResources() {
        this.consumeWarehouseResources = true;
    }
}
