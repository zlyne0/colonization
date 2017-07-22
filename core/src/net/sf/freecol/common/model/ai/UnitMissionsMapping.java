package net.sf.freecol.common.model.ai;

import com.badlogic.gdx.utils.ObjectIntMap;

import net.sf.freecol.common.model.Unit;

public class UnitMissionsMapping {
	
	private final ObjectIntMap<String> unitMissions = new ObjectIntMap<String>();

	public void blockUnit(Unit unit, AbstractMission mission) {
		unitMissions.getAndIncrement(unit.getId(), 0, 1);
	}
	
	public boolean isUnitInMission(Unit unit) {
		return unitMissions.get(unit.getId(), 0) > 0;
	}
	
	public void unblockUnitFromMission(Unit unit, AbstractMission mission) {
		unitMissions.getAndIncrement(unit.getId(), 0, -1);
	}
	
}
