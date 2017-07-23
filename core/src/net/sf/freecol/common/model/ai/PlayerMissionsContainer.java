package net.sf.freecol.common.model.ai;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.player.Player;

public class PlayerMissionsContainer extends ObjectWithId {

	private final MapIdEntities<AbstractMission> missions = MapIdEntities.linkedMapIdEntities();
	private final Player player;
	private final UnitMissionsMapping unitMissionsMapping = new UnitMissionsMapping();
	
	public PlayerMissionsContainer(Player player) {
		super(player.getId());
		this.player = player;
	}

	public void addMission(AbstractMission m) {
		missions.add(m);
	}

	public void clearDoneMissions() {
		List<AbstractMission> l = new ArrayList<AbstractMission>(missions.entities());
		for (AbstractMission am : l) {
			if (am.isDone() && !am.hasDependMissions()) {
				missions.removeId(am);
			}
		}
	}

	public boolean hasMissionType(Class<? extends AbstractMission> clazz) {
		for (AbstractMission am : missions.entities()) {
			if (am.getClass() == clazz) {
				return true;
			}
			if (am.hasDependMissionsType(clazz)) {
				return true;
			}
		}
		return false;
	}
	
	public MapIdEntities<AbstractMission> getMissions() {
		return missions;
	}

	public void blockUnitForMission(Unit unit, AbstractMission mission) {
		unitMissionsMapping.blockUnit(unit, mission);
	}
	
	public void blockUnitsForMission(AbstractMission mission) {
		mission.blockUnits(unitMissionsMapping);
	}

	public void unblockUnitFromMission(Unit unit, AbstractMission mission) {
		if (unit == null) {
			return;
		}
		unitMissionsMapping.unblockUnitFromMission(unit, mission);
	}
	
	public void unblockUnitsFromMission(AbstractMission mission) {
		mission.unblockUnits(unitMissionsMapping);
	}

	public boolean isUnitBlockedForMission(Unit unit) {
		return unitMissionsMapping.isUnitInMission(unit);
	}

	public void interruptMission(Unit unit) {
		for (AbstractMission mission : unitMissionsMapping.getUnitMission(unit)) {
			mission.setDone();
			unitMissionsMapping.unblockUnitFromMission(unit, mission);
		}
	}
}
