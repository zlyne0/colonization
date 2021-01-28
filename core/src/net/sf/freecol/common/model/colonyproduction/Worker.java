package net.sf.freecol.common.model.colonyproduction;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.UnitType;

class Worker {
	Identifiable unitId;
	UnitType unitType;
	
	public Worker(Identifiable unitId, UnitType unitType) {
		this.unitId = unitId;
		this.unitType = unitType;
	}
}