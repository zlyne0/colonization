package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;

class Worker {
	Unit unit;
	UnitType unitType;
	
	public Worker(Unit unit, UnitType unitType) {
		this.unit = unit;
		this.unitType = unitType;
	}

	public Worker(UnitType unitType) {
		this.unit = null;
		this.unitType = unitType;
	}
}