package net.sf.freecol.common.model.ai.missions;

import com.badlogic.gdx.utils.ObjectIntMap;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.pioneer.RequestGoodsMission;
import net.sf.freecol.common.model.ai.missions.transportunit.TransportUnitRequestMission;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.util.Predicate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import promitech.colonization.ai.CommonMissionHandler;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;

import static promitech.colonization.ai.MissionHandlerLogger.logger;

public class TransportUnitMission extends AbstractMission {

	public interface TransportDestination {
	}

	public class CargoDest implements TransportDestination {
		public final Tile dest;
		public final GoodsType goodsType;
		public final int amount;
		public final String requestGoodsMissionId;

		public CargoDest(Tile dest, GoodsType goodsType, int amount, String requestGoodsMissionId) {
			this.dest = dest;
			this.goodsType = goodsType;
			this.amount = amount;
			this.requestGoodsMissionId = requestGoodsMissionId;
		}

		@Override
		public String toString() {
			return "[" + goodsType.getId() + " " + amount + " to " + dest.toPrettyString() + "]";
		}
	}

    public class UnitDest implements TransportDestination {
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
	private final List<CargoDest> cargoDests = new ArrayList<CargoDest>();

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

    public TransportDestination firstTransportOrder() {
    	if (!unitsDest.isEmpty()) {
			return unitsDest.get(0);
		}
    	if (!cargoDests.isEmpty()) {
    		return cargoDests.get(0);
		}
		return null;
    }

	public Collection<Tile> destTiles() {
		Set<Tile> tiles = new HashSet<Tile>(unitsDest.size() + cargoDests.size());
		for (UnitDest ud : unitsDest) {
			tiles.add(ud.dest);
		}
		for (CargoDest cargoDest : cargoDests) {
			tiles.add(cargoDest.dest);
		}
		return tiles;
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
		unitsDest.removeAll(toRemoveList);
		return toRemoveList;
	}

	public void validateUnitDestination() {
		Player player = carrier.getOwner();

		List<UnitDest> toRemove = null;
		for (UnitDest unitDest : unitsDest) {
			if (!player.units.containsId(unitDest.unit) || !unitDest.unit.getOwner().equalsId(player)) {
				if (toRemove == null) {
					toRemove = new ArrayList<UnitDest>();
				}
				toRemove.add(unitDest);
			}
		}
		if (toRemove != null) {
			unitsDest.removeAll(toRemove);
		}
	}

	public List<CargoDest> unloadCargo(Tile tile) {
		if (cargoDests.isEmpty()) {
			return Collections.emptyList();
		}
		Settlement settlement = tile.getSettlement();

		List<CargoDest> toRemoveList = null;
		for (CargoDest cargoDest : cargoDests) {
			if (cargoDest.dest.equalsCoordinates(tile)) {
				if (toRemoveList == null) {
					toRemoveList = new ArrayList<CargoDest>(cargoDests.size());
				}
				toRemoveList.add(cargoDest);

				carrier.getGoodsContainer().transferGoods(
					cargoDest.goodsType,
					cargoDest.amount,
					settlement.getGoodsContainer()
				);
			}
		}
		if (toRemoveList != null) {
			cargoDests.removeAll(toRemoveList);
			return toRemoveList;
		} else {
			return Collections.emptyList();
		}
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
		int slotsTaken = carrier.getGoodsContainer().getCargoSpaceTaken();
		for (UnitDest unitDest : unitsDest) {
			if (!carrier.getUnitContainer().isContainUnit(unitDest.unit)) {
				slotsTaken += unitDest.unit.unitType.getSpaceTaken();
			}
		}
		return slotsTaken;
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

	public boolean isDestinationEmpty() {
    	return unitsDest.isEmpty() && cargoDests.isEmpty();
	}

    public List<UnitDest> getUnitsDest() {
        return unitsDest;
    }

	public void addCargoDest(Tile tile, GoodsType goodsType, int amount) {
		cargoDests.add(new CargoDest(tile, goodsType, amount, null));
	}

	public void addCargoDest(Tile tile, GoodsType goodsType, int amount, String requestGoodsMissionId) {
		cargoDests.add(new CargoDest(tile, goodsType, amount, requestGoodsMissionId));
	}

	public CargoDest addCargoDest(CargoDest cargoDest, Tile newDestination) {
		CargoDest newCargoDest = new CargoDest(newDestination, cargoDest.goodsType, cargoDest.amount, null);
		cargoDests.add(newCargoDest);
		return newCargoDest;
	}

	public void addCargoDest(Player player, RequestGoodsMission transportRequestMission) {
		for (ObjectIntMap.Entry<GoodsType> goodsEntry : transportRequestMission.getGoodsCollection()) {
			Settlement settlement = player.settlements.getByIdOrNull(transportRequestMission.getColonyId());
			if (settlement != null) {
				cargoDests.add(new CargoDest(settlement.tile, goodsEntry.key, goodsEntry.value, transportRequestMission.getId()));
			}
		}
	}

	public Tile carrierPosition(Map map) {
		Tile sourceTile = null;
		if (carrier.isAtHighSeasLocation() || carrier.isAtEuropeLocation()) {
			if (carrier.getEnterHighSea() != null) {
				sourceTile = map.getSafeTile(carrier.getEnterHighSea());
			} else {
				sourceTile = map.getSafeTile(carrier.getOwner().getEntryLocation());
			}
		}
		if (carrier.isAtTileLocation()) {
			sourceTile = carrier.getTile();
		}
		return sourceTile;
	}

	public TransportDestination firstTransportDestinationForTile(Tile tile) {
		for (UnitDest unitDest : unitsDest) {
			if (unitDest.dest.equalsCoordinates(tile)) {
				return unitDest;
			}
		}
		for (CargoDest cargoDest : cargoDests) {
			if (cargoDest.dest.equalsCoordinates(tile)) {
				return cargoDest;
			}
		}
		return null;
	}

	public List<CargoDest> getCargoDests() {
		return cargoDests;
	}

	public boolean isNotLoaded(RequestGoodsMission transportRequestMission) {
		for (CargoDest cargoDest : cargoDests) {
			if (transportRequestMission.equalsId(cargoDest.requestGoodsMissionId)) {
				return false;
			}
		}
		return true;
	}

	public static class Xml extends AbstractMission.Xml<TransportUnitMission> {

        private static final String ATTR_CARRIER = "carrier";
        private static final String ELEMENT_UNIT = "unitDest";
        private static final String DEST_UNIT = "unit";
        private static final String DEST_TILE = "tile";
        private static final String DEST_ALLOW_UNIT_MOVE = "allowMove";
        private static final String TRANSPORT_REQUEST_ID = "transportRequestId";

        private static final String ELEMENT_CARGO = "cargoDest";
		private static final String GOODS_TYPE = "goodsType";
		private static final String AMOUNT = "amount";
		private static final String REQUEST_GOODS_MISSION = "requestGoodsMissionId";

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
            if (attr.isQNameEquals(ELEMENT_CARGO)) {
				Tile tile = game.map.getSafeTile(attr.getPoint(DEST_TILE));
				GoodsType goodsType = attr.getEntity(GOODS_TYPE, Specification.instance.goodsTypes);
				int amount = attr.getIntAttribute(AMOUNT);
				String requestGoodsMissionId = attr.getStrAttribute(REQUEST_GOODS_MISSION);
				nodeObject.addCargoDest(tile, goodsType, amount, requestGoodsMissionId);
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

			for (CargoDest cargoDest : mission.cargoDests) {
				attr.xml.element(ELEMENT_CARGO);
				attr.setPoint(DEST_TILE, cargoDest.dest.x, cargoDest.dest.y);
				attr.set(GOODS_TYPE, cargoDest.goodsType);
				attr.set(AMOUNT, cargoDest.amount);
				attr.set(REQUEST_GOODS_MISSION, cargoDest.requestGoodsMissionId);
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