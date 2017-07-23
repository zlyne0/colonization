package net.sf.freecol.common.model.ai.missions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.ai.UnitMissionsMapping;

public abstract class AbstractMission extends ObjectWithId {
	private boolean done = false;
	private List<AbstractMission> dependMissions = new ArrayList<AbstractMission>();
	
	public AbstractMission(String id) {
		super(id);
	}

	public abstract void blockUnits(UnitMissionsMapping unitMissionsMapping);
	public abstract void unblockUnits(UnitMissionsMapping unitMissionsMapping);
	
	public void addDependMission(AbstractMission m) {
		dependMissions.add(m);
	}

	public void setDone() {
		done = true;
	}

	public boolean isDone() {
		return done;
	}
	
	public LinkedList<AbstractMission> getLeafMissionToExecute() {
	    LinkedList<AbstractMission> leafs = new LinkedList<AbstractMission>();
	    
	    for (AbstractMission am : dependMissions) {
	        if (am.isDone()) {
	            continue;
	        }
	        if (am.hasDependMissions()) {
	            leafs.addAll(am.getLeafMissionToExecute());
	        } else {
	            leafs.add(am);
	        }
	    }
	    return leafs;
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

	public boolean hasDependMissionsType(Class<? extends AbstractMission> clazz) {
		if (dependMissions.isEmpty()) {
			return false;
		}
		for (AbstractMission am : dependMissions) {
			if (am.getClass() == clazz) {
				return true;
			}
			if (am.hasDependMissionsType(clazz)) {
				return true;
			}
		}
		return false;
	}

}