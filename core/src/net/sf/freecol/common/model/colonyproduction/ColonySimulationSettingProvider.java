package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyLiberty;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.Modifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColonySimulationSettingProvider implements ColonySettingProvider {
    private final DefaultColonySettingProvider defaultColonySettingProvider;
    private final List<ColonyTileProduction> additionalTileProduction = new ArrayList<ColonyTileProduction>(9);
    private final Map<String, List<UnitType>> additionalBuildingProduction = new HashMap<String, List<UnitType>>();
    private int additionalWorkers = 0;

    private final ColonyLiberty colonyLiberty = new ColonyLiberty();
    private Modifier productionBonus = new Modifier(Modifier.COLONY_PRODUCTION_BONUS, Modifier.ModifierType.ADDITIVE, 0);
    private boolean consumeWarehouseResources = false;

    public ColonySimulationSettingProvider(Colony colony) {
        defaultColonySettingProvider = new DefaultColonySettingProvider(colony);
        colonyLiberty.copy(colony.colonyLiberty);
    }

    @Override
    public void init(MapIdEntities<ColonyTileProduction> tiles, MapIdEntities<BuildingProduction> buildings, List<Worker> workers) {
        colonyLiberty.updateSonOfLiberty(
            defaultColonySettingProvider.colony.getOwner(),
            defaultColonySettingProvider.colony.getColonyUnitsCount() + additionalWorkers
        );
        productionBonus.setValue(colonyLiberty.productionBonus());

        defaultColonySettingProvider.init(tiles, buildings, workers);

        for (ColonyTileProduction colonyTileProduction : additionalTileProduction) {
            tiles.add(colonyTileProduction);
            colonyTileProduction.addWorker(workers);
        }

        for (Map.Entry<String, List<UnitType>> entry : additionalBuildingProduction.entrySet()) {
            BuildingProduction bprod = buildings.getByIdOrNull(entry.getKey());
            if (bprod == null) {
                bprod = new BuildingProduction(Specification.instance.buildingTypes.getById(entry.getKey()));
                buildings.add(bprod);
            }
            bprod.addWorkers(entry.getValue(), workers);
        }
    }

    @Override
    public void initWarehouse(Warehouse warehouse) {
        if (this.consumeWarehouseResources) {
            defaultColonySettingProvider.initWarehouse(warehouse);
        } else {
            warehouse.reset(defaultColonySettingProvider.colony.warehouseCapacity());
        }
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
    public boolean isCenterTile(Tile tile) {
        return defaultColonySettingProvider.isCenterTile(tile);
    }

    @Override
    public boolean isTileLocked(Tile tile, boolean ignoreIndianOwner) {
        return defaultColonySettingProvider.isTileLocked(tile, ignoreIndianOwner);
    }

    public void addWorkerToColony(UnitType workerType, MaxGoodsProductionLocation maxProd) {
        additionalWorkers++;
        if (maxProd.colonyTile != null) {
            ColonyTileProduction colonyTileProduction = new ColonyTileProduction(maxProd.colonyTile);
            colonyTileProduction.init(maxProd.tileTypeInitProduction, workerType);
            additionalTileProduction.add(colonyTileProduction);
        }
        if (maxProd.buildingType != null) {
            List<UnitType> list = additionalBuildingProduction.get(maxProd.buildingType.getId());
            if (list == null) {
                list = new ArrayList<UnitType>();
                additionalBuildingProduction.put(maxProd.buildingType.getId(), list);
            }
            list.add(workerType);
        }
    }

    public void addWorkerToColony(UnitType workerType, BuildingType buildingType) {
        additionalWorkers++;
        List<UnitType> list = additionalBuildingProduction.get(buildingType.getId());
        if (list == null) {
            list = new ArrayList<UnitType>();
            additionalBuildingProduction.put(buildingType.getId(), list);
        }
        list.add(workerType);
    }

    public void withConsumeWarehouseResources() {
        this.consumeWarehouseResources = true;
    }
}
