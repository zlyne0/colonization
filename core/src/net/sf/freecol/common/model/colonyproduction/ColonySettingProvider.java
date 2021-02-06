package net.sf.freecol.common.model.colonyproduction;

import java.util.List;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.specification.Modifier;

class ColonySettingProvider {

	private final Colony colony;

	public ColonySettingProvider(Colony colony) {
		this.colony = colony;
	}
	
	void init(List<ColonyTileProduction> tiles, List<BuildingProduction> buildings, List<Worker> workers) {
		tiles.clear();
		buildings.clear();
		workers.clear();
		
		for (ColonyTile colonyTile : colony.colonyTiles) {
			ColonyTileProduction tileProd = new ColonyTileProduction();
			if (colonyTile.getWorker() != null) {
				tileProd.init(colonyTile.tile, colonyTile.tileProduction(), colonyTile.getWorker(), workers);
			} else {
				tileProd.init(colonyTile.tile, colonyTile.tileProduction(), null, workers);
			}
			tiles.add(tileProd);
		}
		
		for (Building building : colony.buildings.sortedEntities()) {
			BuildingProduction buildingProduction = new BuildingProduction();
			buildingProduction.init(building, building.buildingType, building.getUnits(), workers);
			buildings.add(buildingProduction);
		}
	}

	public Modifier productionBonus() {
		return colony.productionBonus();
	}

	public ObjectWithFeatures colonyUpdatableFeatures() {
		return colony.colonyUpdatableFeatures;
	}

	public void initWarehouse(Warehouse warehouse) {
		warehouse.reset(colony);
	}

	public boolean isCenterTile(Tile tile) {
        return tile.getId().equals(colony.tile.getId());
	}

	public boolean isTileLocked(Tile tile, boolean ignoreIndianOwner) {
		return colony.isTileLocked(tile, ignoreIndianOwner);
	}
	
}
