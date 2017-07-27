package net.sf.freecol.common.model.ai.missions;

import net.sf.freecol.common.model.Unit;

public class ExplorerMission extends AbstractMission {

	public final Unit unit;
	
	public ExplorerMission(Unit unit) {
		super(unit.getId());
		this.unit = unit;
	}
	
	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(unit, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(unit, this);
	}

}
