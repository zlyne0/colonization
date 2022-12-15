package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.Modifier;

import java.util.ArrayList;
import java.util.List;

public class DefaultColonySettingProvider implements ColonySettingProvider {

    protected final Colony colony;
    private final Warehouse warehouse = new Warehouse();

    private final MapIdEntities<ColonyTileProduction> tiles;
    private final MapIdEntities<BuildingProduction> buildings;
    private final List<Worker> workers = new ArrayList<Worker>();

    public DefaultColonySettingProvider(Colony colony) {
        this.colony = colony;

        tiles = new MapIdEntities<ColonyTileProduction>();
        buildings = MapIdEntities.linkedMapIdEntities(Specification.instance.buildingTypes.size());
    }

    @Override
    public MapIdEntities<ColonyTileProduction> tiles() {
        return tiles;
    }

    @Override
    public MapIdEntities<BuildingProduction> buildings() {
        return buildings;
    }

    @Override
    public List<Worker> workers() {
        return workers;
    }

    @Override
    public Warehouse warehouse() {
        return warehouse;
    }

    @Override
    public void initProductionLocations() {
        warehouse.reset(colony);

        workers.clear();

        for (ColonyTile colonyTile : colony.colonyTiles) {
            ColonyTileProduction tileProd = colonyTileProduction(colonyTile.tile);
            if (colonyTile.getWorker() != null) {
                tileProd.init(colonyTile.tileProduction(), colonyTile.getWorker());
            } else {
                tileProd.init(colonyTile.tileProduction());
            }
            tileProd.sumWorkers(workers);
        }

        buildings.clear();
        for (Building building : colony.buildings.sortedEntities()) {
            BuildingProduction buildingProduction = new BuildingProduction(building.buildingType);
            buildingProduction.initWorkers(building.getUnits(), workers);
            buildings.add(buildingProduction);
        }
    }

    private BuildingProduction buildingProduction(BuildingType buildingType) {
        BuildingProduction prod = buildings.getByIdOrNull(buildingType);
        if (prod == null) {
            prod = new BuildingProduction(buildingType);
            buildings.add(prod);
        }
        return prod;
    }

    private ColonyTileProduction colonyTileProduction(Tile tile) {
        ColonyTileProduction tileProd = tiles.getByIdOrNull(tile.getId());
        if (tileProd == null) {
            tileProd = new ColonyTileProduction(tile);
            tiles.add(tileProd);
        }
        return tileProd;
    }

    @Override
    public Modifier productionBonus() {
        return colony.productionBonus();
    }

    @Override
    public ObjectWithFeatures colonyUpdatableFeatures() {
        return colony.colonyUpdatableFeatures;
    }

    @Override
    public boolean isCenterTile(Tile tile) {
        return tile.getId().equals(colony.tile.getId());
    }

    @Override
    public boolean isTileLocked(Tile tile, boolean ignoreIndianOwner) {
        return colony.isTileLocked(tile, ignoreIndianOwner);
    }

    void addWorker(BuildingType buildingType, UnitType workerType) {
        BuildingProduction buildingProduction = buildingProduction(buildingType);
        buildingProduction.addWorker(workerType, workers);
    }

    void addWorker(BuildingType buildingType, Unit worker) {
        BuildingProduction buildingProduction = buildingProduction(buildingType);
        buildingProduction.addWorker(worker, workers);
    }

    void addWorker(Tile tile, UnitType workerType, Production production) {
        ColonyTileProduction tileProd = colonyTileProduction(tile);
        tileProd.init(production, workerType);
        tileProd.sumWorkers(workers);
    }

    void addWorker(Tile tile, Unit worker, Production production) {
        ColonyTileProduction tileProd = colonyTileProduction(tile);
        tileProd.init(production, worker);
        tileProd.sumWorkers(workers);
    }

    void clearAllProductionLocations() {
        for (ColonyTileProduction colonyTileProduction : tiles) {
            colonyTileProduction.clearWorkersAllocation();
        }
        for (BuildingProduction buildingProduction : buildings) {
            buildingProduction.removeWorkers();
        }
        workers.clear();
    }

    public void printAllocation() {
        String str = "";
        for (ColonyTileProduction tileProduction : tiles) {
            str += tileProduction.toString() + "\n";
        }
        for (BuildingProduction buildingProduction : buildings) {
            str += buildingProduction.toString() + "\n";
        }
        System.out.println("colony " + colony.getName() + " units allocation \n" + str);
    }

    void putWorkersToColonyViaAllocation() {
        for (Unit unit : colony.settlementWorkers()) {
            unit.changeUnitLocation(colony.tile);
            unit.canChangeState(Unit.UnitState.SKIPPED);
        }
        for (ColonyTileProduction tileProduction : tiles) {
            tileProduction.assignWorkerToColony(colony);
        }
        for (BuildingProduction buildingProduction : buildings) {
            buildingProduction.assignWorkersToColony(colony);
        }
        colony.updateModelOnWorkerAllocationOrGoodsTransfer();
        colony.updateColonyPopulation();
    }

    public void addBuilding(BuildingType buildingType) {
        if (buildingType.getUpgradesFrom() != null) {
            BuildingProduction upgradesFromProduction = buildings.getByIdOrNull(buildingType.getUpgradesFrom());
            if (upgradesFromProduction != null) {
                BuildingProduction newBuildingProduction = upgradesFromProduction.createUpgradeProduction(buildingType);
                buildings.removeId(upgradesFromProduction);
                buildings.add(newBuildingProduction);
            } else {
                buildings.add(new BuildingProduction(buildingType));
            }
        } else {
            buildings.add(new BuildingProduction(buildingType));
        }
    }
}
