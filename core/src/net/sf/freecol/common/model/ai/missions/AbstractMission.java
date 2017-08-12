package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.freecol.common.model.ObjectWithId;
import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.ObjectFromNodeSetter.ChildObject2XmlCustomeHandler;
import promitech.colonization.savegame.XmlNodeParser;

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

	public AbstractMission getDependMissionById(String missionId) {
		for (AbstractMission am : dependMissions) {
			if (am.equalsId(missionId)) {
				return am;
			}
		}
		return null;
	}
	
	public static abstract class Xml<AM extends AbstractMission> extends XmlNodeParser<AM> {

		public Xml() {
			ObjectFromNodeSetter setter = new ObjectFromNodeSetter() {
				@Override
				public void set(Object target, Object entity) {
					((AbstractMission)target).dependMissions.add((AbstractMission)entity);
				}

				@Override
				public void generateXml(Object source, ChildObject2XmlCustomeHandler xmlGenerator) throws IOException {
				}
			}; 
			
			addNode(WanderMission.class, setter);
			addNode(TransportUnitMission.class, setter);
			addNode(RellocationMission.class, setter);
			addNode(FoundColonyMission.class, setter);
			addNode(ExplorerMission.class, setter);
		}

		@Override
		public <COLL_OBJECTY_TYPE> void writeCollections(
			AM entity,
			ChildObject2XmlCustomeHandler<COLL_OBJECTY_TYPE> xmlGenerator
		) throws IOException 
		{
			for (AbstractMission am : entity.dependMissions) {
				xmlGenerator.generateXml((COLL_OBJECTY_TYPE) am);
			}
		}
	}
	
}