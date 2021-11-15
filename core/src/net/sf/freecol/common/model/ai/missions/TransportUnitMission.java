package net.sf.freecol.common.model.ai.missions;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
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
        public final boolean allowUnitMove;

        public UnitDest(Unit unit, Tile dest, boolean allowUnitMove) {
            this.unit = unit;
            this.dest = dest;
            this.allowUnitMove = allowUnitMove;
        }

		@Override
		public String toString() {
			String tileStr = "[" + dest.toStringCords() + "]";
			if (dest.hasSettlement()) {
				tileStr += " " + dest.getSettlement().getName();
			}
			return "[" + unit.getId() + " " + unit.unitType.toSmallIdStr() + " to " + tileStr + "]";
		}
	}

	private Unit carrier;
    private final List<UnitDest> unitsDest = new ArrayList<UnitDest>();

    private TransportUnitMission(String id) {
        super(id);
    }
	
	public TransportUnitMission(Unit carrier) {
        super(Game.idGenerator.nextId(TransportUnitMission.class));
        this.carrier = carrier;
	}

	public TransportUnitMission addUnitDest(Unit unit, Tile tile) {
        this.unitsDest.add(new UnitDest(unit, tile, false));
        return this;
    }

	public TransportUnitMission addUnitDest(Unit unit, Tile tile, boolean allowUnitMove) {
		this.unitsDest.add(new UnitDest(unit, tile, allowUnitMove));
		return this;
	}

    public UnitDest firstUnitToTransport() {
    	if (unitsDest.isEmpty()) {
    		return null;
		}
		return unitsDest.get(0);
    }

    public List<Tile> destTiles() {
    	Set<Tile> tiles = new HashSet<Tile>(unitsDest.size());
    	for (UnitDest ud : unitsDest) {
    		tiles.add(ud.dest);
    	}
    	return new ArrayList<Tile>(tiles);
    }
    
	public boolean canEmbarkUnit(Unit unit) {
		return carrier.freeUnitsSlots() - unitsDest.size() - unit.unitType.getSpaceTaken() >= 0;
	}
    
	public boolean isTransportUnitExists(Player player) {
		return CommonMissionHandler.isUnitExists(player, carrier);
	}
	
	public boolean isTransportedUnit(Unit unit) {
		for (UnitDest unitDest : unitsDest) {
			if (unitDest.unit.equalsId(unit)) {
				return true;
			}
		}
		return false;
	}
	
	public void embarkColonistsInEurope() {
		for (UnitDest unitDest : unitsDest) {
			if (unitDest.unit.isAtLocation(Europe.class)) {
				unitDest.unit.embarkTo(carrier);
			}
		}
		carrier.sailUnitToNewWorld();
	}
	
	public List<Unit> unitsToDisembark(Tile location) {
		List<Unit> units = new ArrayList<Unit>(unitsDest.size());
		for (int i = 0; i < unitsDest.size(); i++) {
			UnitDest unitDest = unitsDest.get(i);
			if (location.equalsCoordinates(unitDest.dest)) {
				units.add(unitDest.unit);
			}
		}
		return units;
	}
	
	public void removeUnit(UnitDest unitDest) {
    	unitsDest.remove(unitDest);
	}
	
	public void removeDisembarkedUnits(Player player, Tile destinationLocation, Tile disembarkLocation) {
		StringBuilder logStr = new StringBuilder();
		
		for (UnitDest ud : new ArrayList<UnitDest>(unitsDest)) {
			if (ud.dest.equalsCoordinates(destinationLocation)) {
				unitsDest.remove(ud);
				
				if (logStr.length() != 0) {
					logStr.append(", ");
				}
				logStr.append(ud.unit.getId());
			}
		}
		logger.debug("player[%s].TransportUnitMissionHandler.disembark units[%s] to tile[%s] moved to [%s]",
			player.getId(), 
			logStr.toString(),
			disembarkLocation.toStringCords(),
			disembarkLocation.toStringCords()
		);
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
    	return "carrier[" + carrier.getId() + ", " + carrier.unitType.toSmallIdStr() + "] " + unitsDestListToString();
	}

	private String unitsDestListToString() {
    	String str = "";
		for (UnitDest unitDest : unitsDest) {
			if (!str.isEmpty()) {
				str += ", ";
			}
			str += unitDest.toString();
		}
		return str;
	}

    public List<UnitDest> getUnitsDest() {
        return unitsDest;
    }

    public static class Xml extends AbstractMission.Xml<TransportUnitMission> {

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