package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.List;

class AIMissionsContainer {
	final List<AbstractMission> missions = new ArrayList<AbstractMission>();

	public void addMission(AbstractMission m) {
		missions.add(m);
	}

	public void clearDoneMissions() {
		List<AbstractMission> l = new ArrayList<AbstractMission>(missions);
		for (AbstractMission am : l) {
			if (am.isDone() && !am.hasDependMissions()) {
				missions.remove(am);
			}
		}
	}
}