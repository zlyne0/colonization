package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitType;

class TileUnitType {
	final Tile tile;
	final UnitType unitType;
	
	public TileUnitType(Tile tile, UnitType unitType) {
		this.tile = tile;
		this.unitType = unitType;
	}
}