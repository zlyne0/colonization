package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMission;
import net.sf.freecol.common.model.ai.missions.indian.DemandTributeMission;
import net.sf.freecol.common.model.ai.missions.indian.IndianBringGiftMission;
import net.sf.freecol.common.model.ai.missions.indian.WanderMission;
import net.sf.freecol.common.model.ai.missions.pioneer.PioneerMission;
import net.sf.freecol.common.model.ai.missions.pioneer.RequestGoodsMission;
import net.sf.freecol.common.model.ai.missions.scout.ScoutMission;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerMission;

import promitech.colonization.savegame.ObjectFromNodeSetter;
import promitech.colonization.savegame.ObjectFromNodeSetter.ChildObject2XmlCustomeHandler;
import promitech.colonization.savegame.XmlNodeParser;

public abstract class AbstractMission extends ObjectWithId {
	private boolean done = false;

	protected List<AbstractMission> dependMissions = new ArrayList<AbstractMission>();
	
	protected AbstractMission(String id) {
		super(id);
	}

	public boolean is(Class<? extends AbstractMission> missionClass) {
		return this.getClass().isAssignableFrom(missionClass);
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
			if (!am.done) {
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

	AbstractMission findParentForMission(AbstractMission m) {
		for (AbstractMission childMission : dependMissions) {
			if (childMission.equalsId(m)) {
				return this;
			}
			AbstractMission localParent = childMission.findParentForMission(m);
			if (localParent != null) {
				return localParent;
			}
		}
		return null;
	}

	public <T extends AbstractMission> T findDependMissionById(String missionId) {
		for (AbstractMission am : dependMissions) {
			if (am.equalsId(missionId)) {
				return (T)am;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "TODO.mission.toString: " + this.getClass().getName() + " " + this.getId();
	}

	public List<AbstractMission> getDependMissions() {
		return dependMissions;
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
			addNode(ExplorerMission.class, setter);
			addNode(IndianBringGiftMission.class, setter);
			addNode(DemandTributeMission.class, setter);
			addNode(TransportGoodsToSellMission.class, setter);
			addNode(ColonyWorkerMission.class, setter);
			addNode(ScoutMission.class, setter);
			addNode(PioneerMission.class, setter);
			addNode(RequestGoodsMission.class, setter);
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