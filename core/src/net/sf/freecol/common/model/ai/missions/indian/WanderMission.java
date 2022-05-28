package net.sf.freecol.common.model.ai.missions.indian;

import java.io.IOException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.UnitMissionsMapping;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer.Xml;
import promitech.colonization.Direction;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

public class WanderMission extends AbstractMission {

	public final Unit unit;
	public Direction previewDirection;
	
	private WanderMission(String id, Unit unit) {
	    super(id);
	    this.unit = unit;
	}
	
	public WanderMission(Unit unit) {
		super(Game.idGenerator.nextId(WanderMission.class));
		this.unit = unit;
	}

	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(unit, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(unit, this);
	}
	
	public String toString() {
		return "WanderMission unit: " + unit + ", previewDirection: " + previewDirection;
	}
	
	public static class Xml extends AbstractMission.Xml<WanderMission> {

        private static final String ATTR_UNIT = "unit";

        @Override
		public void startElement(XmlNodeAttributes attr) {
		    String unitId = attr.getStrAttributeNotNull(ATTR_UNIT);
		    Unit unit = PlayerMissionsContainer.Xml.getPlayerUnit(unitId);
		    
		    WanderMission m = new WanderMission(attr.getId(), unit);
		    nodeObject = m;
			super.startElement(attr);
		}

		@Override
		public void startWriteAttr(WanderMission m, XmlNodeAttributesWriter attr) throws IOException {
			super.startWriteAttr(m, attr);
		    attr.setId(m);
		    attr.set(ATTR_UNIT, m.unit);
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "wanderMission";
		}
		
	}
	
}
