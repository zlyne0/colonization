package net.sf.freecol.common.model.ai;

import java.util.HashSet;
import java.util.Set;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.util.MapList;

class ColonyWorkersSnapshot {
	private final MapList<String, String> unitsByLocation = new MapList<>();
	private final Set<String> units = new HashSet<>();
	private final ProductionSummary production;
	
	public ColonyWorkersSnapshot(Colony colony) {
		production = colony.productionSummary().cloneGoods();
		
		for (Building building : colony.buildings) {
			for (Unit unit : building.getUnits().entities()) {
				unitsByLocation.add(building.getId(), unit.getId());
			}
		}
		for (ColonyTile colonyTile : colony.colonyTiles) {
			if (colonyTile.hasWorker()) {
				unitsByLocation.add(colonyTile.getId(), colonyTile.getWorker().getId());
			}
		}
		
		for (Unit unit : colony.getUnits().entities()) {
			units.add(unit.getId());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((production == null) ? 0 : production.hashCode());
		result = prime * result + ((units == null) ? 0 : units.hashCode());
		result = prime * result + ((unitsByLocation == null) ? 0 : unitsByLocation.hashCode());
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
		ColonyWorkersSnapshot other = (ColonyWorkersSnapshot) obj;
		if (production == null) {
			if (other.production != null)
				return false;
		} else if (!production.equals(other.production))
			return false;
		if (units == null) {
			if (other.units != null)
				return false;
		} else if (!units.equals(other.units))
			return false;
		if (unitsByLocation == null) {
			if (other.unitsByLocation != null)
				return false;
		} else if (!unitsByLocation.equals(other.unitsByLocation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ColonyWorkersSnapshot [unitsByLocation=" + unitsByLocation + ", units=" + units + ", production="
				+ production + "]";
	}


}