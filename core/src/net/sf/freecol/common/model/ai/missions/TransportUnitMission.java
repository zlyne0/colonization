package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.ai.CommonMissionHandler;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

public class TransportUnitMission extends AbstractMission {
    public class UnitDest {
        public final Unit unit;
        public final Tile dest;

        public UnitDest(Unit unit, Tile dest) {
            this.unit = unit;
            this.dest = dest;
        }
    }

	private Unit carrier;

    private final List<UnitDest> unitsDest = new ArrayList<UnitDest>();

    private TransportUnitMission(String id) {
        super(id);
    }
	
	public TransportUnitMission(Player player, Unit carrier) {
        super(Game.idGenerator.nextId(TransportUnitMission.class));
	}

	public void addUnitDest(Unit unit, Tile tile) {
        this.unitsDest.add(new UnitDest(unit, tile));
    }

    public UnitDest firstUnitDest() {
        if (unitsDest.size() > 0) {
            return unitsDest.get(0);
        }
        return null;
    }

	public boolean isTransportUnitExists(Player player) {
		return CommonMissionHandler.isUnitExists(player, carrier);
	}
	
	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(carrier, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(carrier, this);
	}

	public Unit getCarrier() {
		return carrier;
	}

	@Override
	public String toString() {
		return "TransportUnitMission";
	}

    public List<UnitDest> getUnitsDest() {
        return unitsDest;
    }

    public static class Xml extends AbstractMission.Xml<TransportUnitMission> {

        private static final String ATTR_DEST = "dest";
        private static final String ATTR_CARRIER = "carrier";
        private static final String ELEMENT_UNIT = "unitDest";
        private static final String DEST_UNIT = "unit";
        private static final String DEST_TILE = "tile";

        @Override
        public void startElement(XmlNodeAttributes attr) {
            TransportUnitMission m = new TransportUnitMission(attr.getId());
            m.carrier = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_CARRIER));
            nodeObject = m;
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
            if (attr.isQNameEquals(ELEMENT_UNIT)) {
                Unit unit = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(DEST_UNIT));
                Tile tile = game.map.getSafeTile(attr.getPoint(DEST_TILE));
                if (unit != null) {
                    nodeObject.addUnitDest(unit, tile);
                }
            }
        }
        
        @Override
        public void startWriteAttr(TransportUnitMission node, XmlNodeAttributesWriter attr) throws IOException {
            attr.setId(node);
            attr.set(ATTR_CARRIER, node.carrier);

            for (UnitDest ud : node.unitsDest) {
                attr.xml.element(ELEMENT_UNIT);
                attr.set(DEST_UNIT, ud.unit.getId());
                attr.setPoint(DEST_TILE, ud.dest.x, ud.dest.y);
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