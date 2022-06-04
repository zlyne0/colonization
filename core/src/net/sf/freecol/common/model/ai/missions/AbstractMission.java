package net.sf.freecol.common.model.ai.missions;

import net.sf.freecol.common.model.ObjectWithId;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public abstract class AbstractMission extends ObjectWithId {
	private boolean done = false;

	protected String parentMissionId;
	protected Set<String> dependMissions2 = Collections.emptySet();

	protected AbstractMission(String id) {
		super(id);
	}

	public boolean is(Class<? extends AbstractMission> missionClass) {
		return this.getClass().isAssignableFrom(missionClass);
	}
	
	public abstract void blockUnits(UnitMissionsMapping unitMissionsMapping);
	public abstract void unblockUnits(UnitMissionsMapping unitMissionsMapping);
	
	public void setDone() {
		done = true;
	}

	public boolean isDone() {
		return done;
	}

	public boolean hasDependMission(String missionId) {
		return dependMissions2.contains(missionId);
	}

	public boolean hasDependMission() {
		return !dependMissions2.isEmpty();
	}

	@Override
	public String toString() {
		return "TODO.mission.toString: " + this.getClass().getName() + " " + this.getId();
	}

	public abstract static class Xml<AM extends AbstractMission> extends XmlNodeParser<AM> {

		public static final String ATTR_PARENT_MISSION_ID = "parentMissionId";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			nodeObject.parentMissionId = attr.getStrAttribute(ATTR_PARENT_MISSION_ID);
		}

		@Override
		public void startWriteAttr(AM node, XmlNodeAttributesWriter attr) throws IOException {
			attr.set(ATTR_PARENT_MISSION_ID, node.parentMissionId);
		}
	}
	
}