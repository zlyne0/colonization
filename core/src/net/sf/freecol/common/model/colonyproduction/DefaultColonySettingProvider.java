package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.Modifier;

import java.util.ArrayList;
import java.util.List;

class DefaultColonySettingProvider implements ColonySettingProvider {

    protected final Colony colony;

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
    public void initProductionLocations() {
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
            buildingProduction.initWorkers(building.getUnits());
            buildingProduction.sumWorkers(workers);
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
    public void initWarehouse(Warehouse warehouse) {
        warehouse.reset(colony);
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

    public void addWorker(List<UnitType> workerType, BuildingType buildingType) {
        BuildingProduction buildingProduction = buildingProduction(buildingType);
        buildingProduction.addWorker(workerType);
        buildingProduction.sumWorkers(workers);
    }

    public void addWorker(ColonyTileProduction atw) {
        ColonyTileProduction ctp = tiles.getById(atw.tile.getId());
        ctp.init(atw);
        ctp.sumWorkers(workers);
    }
}
