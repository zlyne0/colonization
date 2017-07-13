package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractMission {
	private boolean done = false;
	private List<AbstractMission> dependMissions = new ArrayList<AbstractMission>();
	
	public void addDependMission(AbstractMission m) {
		dependMissions.add(m);
	}

	public void setDone() {
		done = true;
	}

	public boolean isDone() {
		return done;
	}
	
	public boolean hasDependMissions() {
		if (dependMissions.isEmpty()) {
			return false;
		}
		
		boolean hasDepend = false;
		for (AbstractMission am : dependMissions) {
			if (am.done == false) {
				hasDepend = true;
				break;
			}
		}
		return hasDepend;
	}

}