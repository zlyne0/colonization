package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.UnitMissionsMapping;
import net.sf.freecol.common.model.player.Player;
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
	
    public static class Xml extends XmlNodeParser<TransportUnitMission> {

        private static final String ATTR_DEST_X = "destX";
        private static final String ATTR_DEST_Y = "destY";
        private static final String ATTR_CARRIER = "carrier";
        private static final String ELEMENT_UNIT = "unit";

        @Override
        public void startElement(XmlNodeAttributes attr) {
            Player player = PlayerMissionsContainer.Xml.player;
            
            TransportUnitMission m = new TransportUnitMission(attr.getId());
            m.carrier = player.units.getById(attr.getStrAttribute(ATTR_CARRIER));
            m.dest = game.map.getTile(
                attr.getIntAttribute(ATTR_DEST_X), 
                attr.getIntAttribute(ATTR_DEST_Y)
            );
            nodeObject = m;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
            if (attr.isQNameEquals(ELEMENT_UNIT)) {
                nodeObject.units.add(PlayerMissionsContainer.Xml.player.units.getById(attr.getId()));
            }
        }
        
        @Override
        public void startWriteAttr(TransportUnitMission node, XmlNodeAttributesWriter attr) throws IOException {
            attr.setId(node);
            attr.set(ATTR_CARRIER, node.carrier);
            attr.set(ATTR_DEST_X, node.dest.x);
            attr.set(ATTR_DEST_Y, node.dest.y);
            
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