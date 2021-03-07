package net.sf.freecol.common.model.colonyproduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.MapIdEntitiesReadOnly;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.Modifier;

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
