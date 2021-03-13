package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.specification.Modifier;

import java.util.List;

// Travel to any part of universe without moving
public interface ColonySettingProvider {

	void initProductionLocations();

	MapIdEntities<ColonyTileProduction> tiles();

	MapIdEntities<BuildingProduction> buildings();

	List<Worker> workers();

	Modifier productionBonus();

	ObjectWithFeatures colonyUpdatableFeatures();

	boolean isCenterTile(Tile tile);

	boolean isTileLocked(Tile tile, boolean ignoreIndianOwner);

	Warehouse warehouse();
}
