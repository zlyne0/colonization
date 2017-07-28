package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.Direction;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

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
	
	public static class Xml extends XmlNodeParser<WanderMission> {

        private static final String ATTR_UNIT = "unit";

        @Override
		public void startElement(XmlNodeAttributes attr) {
		    String unitId = attr.getStrAttributeNotNull(ATTR_UNIT);
		    Unit unit = PlayerMissionsContainer.Xml.getPlayerUnit(unitId);
		    
		    WanderMission m = new WanderMission(attr.getId(), unit);
		    nodeObject = m;
		}

		@Override
		public void startWriteAttr(WanderMission m, XmlNodeAttributesWriter attr) throws IOException {
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
