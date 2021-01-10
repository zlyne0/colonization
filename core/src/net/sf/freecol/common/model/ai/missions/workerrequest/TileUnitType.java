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
	
	@Override
	public String toString() {
		return "tile[" + tile.toStringCords() + "] " + unitType.getId();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tile == null) ? 0 : tile.hashCode());
		result = prime * result + ((unitType == null) ? 0 : unitType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TileUnitType other = (TileUnitType) obj;
		if (tile == null) {
			if (other.tile != null)
				return false;
		} else if (!tile.equals(other.tile)) {
			return false;
		}
		if (unitType == null) {
			if (other.unitType != null)
				return false;
		} else if (!unitType.equals(other.unitType)) {
			return false;
		}
		return true;
	}
}