package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

public class ExplorerMission extends AbstractMission {

    private final String unitId;

    private ExplorerMission(String id, String unitId) {
        super(id);
        this.unitId = unitId;
    }
	
	public ExplorerMission(Unit unit) {
	    this(Game.idGenerator.nextId(ExplorerMission.class), unit.getId());
	}
	
	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(unitId, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(unitId, this);
	}

    public String getUnitId() {
        return unitId;
    }

    public static class Xml extends AbstractMission.Xml<ExplorerMission> {

        private static final String ATTR_UNIT = "unit";

        public Xml() {
        	super();
        }
        
        @Override
        public void startElement(XmlNodeAttributes attr) {
            ExplorerMission m = new ExplorerMission(
                attr.getId(),
                attr.getStrAttribute(ATTR_UNIT)
            );
            nodeObject = m;

            super.startElement(attr);
        }

        @Override
        public void startWriteAttr(ExplorerMission node, XmlNodeAttributesWriter attr) throws IOException {
            super.startWriteAttr(node, attr);

            attr.setId(node);
            attr.set(ATTR_UNIT, node.unitId);
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "explorerMission";
        }
        
    }
	
}
