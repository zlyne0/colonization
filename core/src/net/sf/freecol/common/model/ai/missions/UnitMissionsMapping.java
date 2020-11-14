package net.sf.freecol.common.model.ai.missions;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.freecol.common.model.Unit;

public class UnitMissionsMapping {
	private final Map<String, Set<AbstractMission>> unitMissions = new HashMap<String, Set<AbstractMission>>();

	public void blockUnit(Unit unit, AbstractMission mission) {
		if (unit != null) {
			blockUnit(unit.getId(), mission);
		}
	}
	
	public void blockUnit(String unitId, AbstractMission mission) {
		Set<AbstractMission> missions = unitMissions.get(unitId);
		if (missions == null) {
			missions = new HashSet<AbstractMission>(2);
			unitMissions.put(unitId, missions);
		}
		missions.add(mission);
	}
	
	public boolean isUnitInMission(String unitId) {
		Set<AbstractMission> missions = unitMissions.get(unitId);
		return missions != null && missions.size() > 0;
	}

	public void unblockUnitFromMission(Unit unit, AbstractMission mission) {
		if (unit != null) {
			unblockUnitFromMission(unit.getId(), mission);
		}
	}
	
	public void unblockUnitFromMission(String unitId, AbstractMission mission) {
		Set<AbstractMission> missions = unitMissions.get(unitId);
		if (missions == null) {
			return;
		}
		missions.remove(mission);
	}
	
	public Set<AbstractMission> getUnitMission(Unit unit) {
		Set<AbstractMission> missions = unitMissions.get(unit.getId());
		if (missions == null) {
			return Collections.emptySet();
		}
		return missions;
	}
}
