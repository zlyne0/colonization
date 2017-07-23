package net.sf.freecol.common.model.ai;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;

public class UnitMissionsMapping {
	private final Map<String, Set<AbstractMission>> unitMissions = new HashMap<String, Set<AbstractMission>>();

	public void blockUnit(Unit unit, AbstractMission mission) {
		Set<AbstractMission> missions = unitMissions.get(unit.getId());
		if (missions == null) {
			missions = new HashSet<AbstractMission>(2);
		}
		missions.add(mission);
	}
	
	public boolean isUnitInMission(Unit unit) {
		Set<AbstractMission> missions = unitMissions.get(unit.getId());
		return missions != null && missions.size() > 0;
	}
	
	public void unblockUnitFromMission(Unit unit, AbstractMission mission) {
		Set<AbstractMission> missions = unitMissions.get(unit.getId());
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
