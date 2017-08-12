package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class ExplorerMission extends AbstractMission {

	public final Unit unit;

    public ExplorerMission(String id, Unit unit) {
        super(id);
        this.unit = unit;
    }
	
	public ExplorerMission(Unit unit) {
	    this(Game.idGenerator.nextId(ExplorerMission.class), unit);
	}
	
	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(unit, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(unit, this);
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
                PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_UNIT))
            );
            nodeObject = m;
        }

        @Override
        public void startWriteAttr(ExplorerMission node, XmlNodeAttributesWriter attr) throws IOException {
            attr.setId(node);
            attr.set(ATTR_UNIT, node.unit);
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
