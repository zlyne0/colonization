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
import net.sf.freecol.common.util.Predicate;

import promitech.colonization.ai.CommonMissionHandler;
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

public class TransportUnitMission extends AbstractMission {
    public class UnitDest {
        public final Unit unit;
        public final Tile dest;
        public final boolean allowUnitMove;
        public final String transportRequestMissionId;

        public UnitDest(Unit unit, Tile dest, boolean allowUnitMove, String transportRequestMissionId) {
            this.unit = unit;
            this.dest = dest;
            this.allowUnitMove = allowUnitMove;
            this.transportRequestMissionId = transportRequestMissionId;
        }

		@Override
		public String toString() {
			return "[" + unit.getId() + " " + unit.unitType.toSmallIdStr() + " to " + dest.toPrettyString() + "]";
		}
	}

	public static boolean isUnitExistsOnTransportMission(final PlayerMissionsContainer playerMissionContainer, final Unit unit) {
		return playerMissionContainer.hasMission(TransportUnitMission.class, new Predicate<TransportUnitMission>() {
			@Override
			public boolean test(TransportUnitMission mission) {
				return mission.isTransportedUnit(unit);
			}
		});
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
        this.unitsDest.add(new UnitDest(unit, tile, false, null));
        return this;
    }

	public TransportUnitMission addUnitDest(Unit unit, Tile tile, boolean allowUnitMove) {
		this.unitsDest.add(new UnitDest(unit, tile, allowUnitMove, null));
		return this;
	}

	private TransportUnitMission addUnitDest(Unit unit, Tile tile, boolean allowUnitMove, String transportRequestId) {
		this.unitsDest.add(new UnitDest(unit, tile, allowUnitMove, transportRequestId));
		return this;
	}

	public TransportUnitMission addUnitDest(TransportUnitRequestMission reqTransport) {
		this.unitsDest.add(new UnitDest(reqTransport.getUnit(), reqTransport.getDestination(), true, reqTransport.getId()));
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

	public List<UnitDest> removeNoAccessTileUnits(Player player, Tile destinationLocation) {
		if (logger.isDebug()) {
			String unitsIds = unitsDestListToString(destinationLocation);
			logger.debug("player[%s].TransportUnitMissionHandler.removeNoAccessTileUnits units[%s] to tile[%s]",
				player.getId(),
				unitsIds.toString(),
				destinationLocation.toStringCords()
			);
		}
		return removeUnitDest(destinationLocation);
	}

	public List<UnitDest> removeDisembarkedUnits(Player player, Tile destinationLocation, Tile disembarkLocation) {
		if (logger.isDebug()) {
			String unitsIds = unitsDestListToString(destinationLocation);
			logger.debug("player[%s].TransportUnitMissionHandler.disembark units[%s] to tile[%s] moved to [%s]",
				player.getId(),
				unitsIds.toString(),
				disembarkLocation.toStringCords(),
				destinationLocation.toStringCords()
			);
		}
		return removeUnitDest(destinationLocation);
	}

	private List<UnitDest> removeUnitDest(Tile destinationLocation) {
		List<UnitDest> toRemoveList = new ArrayList<UnitDest>(unitsDest.size());
		for (UnitDest ud : unitsDest) {
			if (ud.dest.equalsCoordinates(destinationLocation)) {
				toRemoveList.add(ud);
			}
		}
		for (UnitDest ud : toRemoveList) {
			unitsDest.remove(ud);
		}
		return toRemoveList;
	}

	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(carrier, this);
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(carrier, this);
	}

	public int spaceTakenByUnits() {
		int freeUnitsSlots = 0;
		for (UnitDest unitDest : unitsDest) {
			if (!carrier.getUnitContainer().isContainUnit(unitDest.unit)) {
				freeUnitsSlots += unitDest.unit.unitType.getSpaceTaken();
			}
		}
		return freeUnitsSlots;
	}

	public boolean isCarrier(Unit unit) {
    	return this.carrier.equalsId(unit);
	}

	public Unit getCarrier() {
		return carrier;
	}

	@Override
	public String toString() {
    	return "carrier[" + carrier.getId() + ", " + carrier.unitType.toSmallIdStr() + "] " + unitsDestListToString();
	}

	private String unitsDestListToString(Tile destinationLocation) {
		StringBuilder logStr = new StringBuilder();

		for (UnitDest ud : unitsDest) {
			if (ud.dest.equalsCoordinates(destinationLocation)) {
				if (logStr.length() != 0) {
					logStr.append(", ");
				}
				logStr.append(ud.unit.getId());
			}
		}
		return logStr.toString();
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

	public boolean isEmptyUnitsDest() {
    	return unitsDest.isEmpty();
	}

    public List<UnitDest> getUnitsDest() {
        return unitsDest;
    }

    public static class Xml extends AbstractMission.Xml<TransportUnitMission> {

        private static final String ATTR_CARRIER = "carrier";
        private static final String ELEMENT_UNIT = "unitDest";
        private static final String DEST_UNIT = "unit";
        private static final String DEST_TILE = "tile";
        private static final String DEST_ALLOW_UNIT_MOVE = "allowMove";
        private static final String TRANSPORT_REQUEST_ID = "transportRequestId";

        @Override
        public void startElement(XmlNodeAttributes attr) {
            TransportUnitMission m = new TransportUnitMission(attr.getId());
            m.carrier = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(ATTR_CARRIER));
            nodeObject = m;
			super.startElement(attr);
        }

        @Override
        public void startReadChildren(XmlNodeAttributes attr) {
            if (attr.isQNameEquals(ELEMENT_UNIT)) {
                Unit unit = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(DEST_UNIT));
                Tile tile = game.map.getSafeTile(attr.getPoint(DEST_TILE));
                if (unit != null) {
                    nodeObject.addUnitDest(
                    	unit,
						tile,
						attr.getBooleanAttribute(DEST_ALLOW_UNIT_MOVE, false),
						attr.getStrAttribute(TRANSPORT_REQUEST_ID)
					);
                }
            }
        }
        
        @Override
        public void startWriteAttr(TransportUnitMission mission, XmlNodeAttributesWriter attr) throws IOException {
			super.startWriteAttr(mission, attr);
			attr.setId(mission);
            attr.set(ATTR_CARRIER, mission.carrier);

            for (UnitDest ud : mission.unitsDest) {
                attr.xml.element(ELEMENT_UNIT);
                attr.set(DEST_UNIT, ud.unit.getId());
                attr.setPoint(DEST_TILE, ud.dest.x, ud.dest.y);
                attr.set(DEST_ALLOW_UNIT_MOVE, ud.allowUnitMove, false);
                attr.set(TRANSPORT_REQUEST_ID, ud.transportRequestMissionId);
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