package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class TransportUnitMission extends AbstractMission {
	public Unit carrier;
	public Tile dest;
	public final MapIdEntities<Unit> units = new MapIdEntities<Unit>();

    private TransportUnitMission(String id) {
        super(id);
    }
	
	public TransportUnitMission() {
		super(Game.idGenerator.nextId(TransportUnitMission.class));
	}
	
	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(carrier, this);
		for (Unit u : units.entities()) {
			unitMissionsMapping.blockUnit(u, this);
		}
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(carrier, this);
		for (Unit u : units.entities()) {
			unitMissionsMapping.unblockUnitFromMission(u, this);
		}
	}
	
	public String toString() {
		return "TransportUnitMission";
	}
	
    public static class Xml extends AbstractMission.Xml<TransportUnitMission> {

        private static final String ATTR_DEST = "dest";
        private static final String ATTR_CARRIER = "carrier";
        private static final String ELEMENT_UNIT = "unit";

        @Override
        public void startElement(XmlNodeAttributes attr) {
            TransportUnitMission m = new TransportUnitMission(attr.getId());
            m.carrier = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_CARRIER));
            m.dest = game.map.getSafeTile(attr.getPoint(ATTR_DEST));
            nodeObject = m;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
            if (attr.isQNameEquals(ELEMENT_UNIT)) {
                nodeObject.units.add(PlayerMissionsContainer.Xml.getPlayerUnit(attr.getId()));
            }
        }
        
        @Override
        public void startWriteAttr(TransportUnitMission node, XmlNodeAttributesWriter attr) throws IOException {
            attr.setId(node);
            attr.set(ATTR_CARRIER, node.carrier);
            attr.setPoint(ATTR_DEST, node.dest.x, node.dest.y);
            
            for (Unit u : node.units.entities()) {
                attr.xml.element(ELEMENT_UNIT);
                attr.setId(u);
                attr.xml.pop();
            }
        }
        
        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "transportUnitMission";
        }
        
    }
	
}