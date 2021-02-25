package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.specification.Modifier;

import java.util.List;

class DefaultColonySettingProvider implements ColonySettingProvider {

    protected final Colony colony;

    public DefaultColonySettingProvider(Colony colony) {
        this.colony = colony;
    }

    @Override
    public void init(MapIdEntities<ColonyTileProduction> tiles, MapIdEntities<BuildingProduction> buildings, List<Worker> workers) {
        buildings.clear();
        workers.clear();

        for (ColonyTile colonyTile : colony.colonyTiles) {
            ColonyTileProduction tileProd = tiles.getByIdOrNull(colonyTile.tile.getId());
            if (tileProd == null) {
                tileProd = new ColonyTileProduction(colonyTile.tile);
                tiles.add(tileProd);
            }
            if (colonyTile.getWorker() != null) {
                tileProd.init(colonyTile.tileProduction(), colonyTile.getWorker(), workers);
            } else {
                tileProd.init(colonyTile.tileProduction(), null, workers);
            }
        }

        for (Building building : colony.buildings.sortedEntities()) {
            BuildingProduction buildingProduction = new BuildingProduction(building.buildingType);
            buildingProduction.init(building.getUnits(), workers);
            buildings.add(buildingProduction);
        }
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

}
