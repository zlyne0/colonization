package net.sf.freecol.common.model.ai.missions;

import java.io.IOException;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.Path;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeAttributesWriter;
import promitech.colonization.savegame.XmlNodeParser;

public class RellocationMission extends AbstractMission {
	public final Tile rellocationDestination;
	
	public final Unit unit;
	public Tile unitDestination;
	
	public Unit carrier;
	public Tile carrierDestination;

	public RellocationMission(String id, Tile rellocationDestination, Unit unit, Unit carrier) {
		super(id);
		
	    this.rellocationDestination = rellocationDestination;
	    this.unit = unit;
	    this.carrier = carrier;
	}
	
	public RellocationMission(Tile rellocationDestination, Unit unit, Unit carrier) {
		this(Game.idGenerator.nextId(RellocationMission.class), rellocationDestination, unit, carrier);
	}

    public RellocationMission(String id, Tile rellocationDestination, Unit unit) {
		this(id, rellocationDestination, unit, null);
    }
    
    public RellocationMission(Tile rellocationDestination, Unit unit) {
		this(Game.idGenerator.nextId(RellocationMission.class), rellocationDestination, unit, null);
    }
	
	@Override
	public void blockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.blockUnit(unit, this);
		if (carrier != null) {
			unitMissionsMapping.blockUnit(carrier, this);
		}
	}

	@Override
	public void unblockUnits(UnitMissionsMapping unitMissionsMapping) {
		unitMissionsMapping.unblockUnitFromMission(unit, this);
		if (carrier != null) {
			unitMissionsMapping.unblockUnitFromMission(carrier, this);
		}
	}
    
	public boolean isUnitOnCarrier() {
		return carrier.getUnits().containsId(unit);
	}
	
	public boolean isUnitOnRellocationDestination() {
		Tile tile = unit.getTileLocationOrNull();
		return tile != null && tile.equalsCoordinates(rellocationDestination);
	}
	
	public boolean isUnitOnStepDestination() {
		Tile tile = unit.getTileLocationOrNull();
		return tile != null && tile.equalsCoordinates(unitDestination);
	}
	
	public boolean isCarrierOnStepDestination() {
		if (carrier == null || carrierDestination == null) {
			return false;
		}
		Tile tile = carrier.getTileLocationOrNull();
		return tile != null && tile.equalsCoordinates(carrierDestination);
	}
	
	public boolean needCarrierMove() {
		if (carrier == null || carrierDestination == null) {
			return false;
		}
		Tile carrierTile = carrier.getTileLocationOrNull();
		if (carrierTile == null) {
			// carrier can be in europe
			return false;
		}
		if (carrierTile.equalsCoordinates(carrierDestination)) {
			return false;
		}
		return carrier.hasMovesPoints();
	}
	
	public boolean needUnitMove() {
		Tile unitTile = unit.getTileLocationOrNull();
		if (unitTile == null) {
			// maybe in carrier
			return false;
		}
		if (unitTile.equalsCoordinates(unitDestination)) {
			return false;
		}
		return true;
	}
	
	public String toString() {
		return "RellocationMission";
	}

	public void initNextStepDestinationFromPath(Path path) {
		unitDestination = null;
		carrierDestination = null;
		
		boolean unitOnLand = unit.getTileLocationOrNull() != null;
		
    	Tile preview = path.tiles.get(0);
    	for (int i = 1; i < path.tiles.size; i++) {
    		Tile t = path.tiles.get(i);
    		if (preview.getType().isLand() && t.getType().isWater()) {
    			//System.out.println("  wait for ship");
    			if (unitOnLand) {
    				unitDestination = preview;
    				carrierDestination = t;
    				return;
    			}
    		}
    		if (preview.getType().isWater() && t.getType().isLand()) {
    			//System.out.println("  disembark");
    			if (!unitOnLand) {
    				unitDestination = t;
    				carrierDestination = preview;
    				return;
    			}
    		}
    		//System.out.println("t = " + t);
    		preview = t;
    	}
    	
    	// all path tiles are on the same land type
    	if (unitOnLand && unitDestination == null && carrierDestination == null) {
    		unitDestination = path.tiles.get(path.tiles.size-1);
    	} else {
    		carrierDestination = path.tiles.get(path.tiles.size-1);
    	}
	}

	public void toStringDebugTileTab(String[][] tilesStr) {
		tilesStr[rellocationDestination.y][rellocationDestination.x] = "DEST";
		if (unitDestination != null) {
			tilesStr[unitDestination.y][unitDestination.x] = "UNIT dest";
		}
		if (carrierDestination != null) {
			tilesStr[carrierDestination.y][carrierDestination.x] = "CARRIER dest";
		}
	}

	public boolean isRequireCarrierToHandleMission() {
		return carrierDestination != null;
	}

    public boolean isRequireGeneratePath() {
        return carrierDestination == null && unitDestination == null;
    }
    
	public static class Xml extends AbstractMission.Xml<RellocationMission> {

		private static final String CARRIER_DEST = "carrierDest";
		private static final String CARRIER = "carrier";
		private static final String UNIT_DEST = "unitDest";
		private static final String UNIT = "unit";
		private static final String DEST = "dest";

		@Override
		public void startElement(XmlNodeAttributes attr) {
			RellocationMission m = new RellocationMission(
				attr.getId(),
				game.map.getSafeTile(attr.getPoint(DEST)), 
				PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(UNIT))
			);
			if (attr.hasAttr(UNIT_DEST)) {
				m.unitDestination = game.map.getSafeTile(attr.getPoint(UNIT_DEST));
			}
			if (attr.hasAttr(CARRIER)) {
				m.carrier = PlayerMissionsContainer.Xml.getPlayerUnit(attr.getStrAttribute(CARRIER));
			}
			if (attr.hasAttr(CARRIER_DEST)) {
				m.carrierDestination = game.map.getSafeTile(attr.getPoint(CARRIER_DEST));
			}
			nodeObject = m;
		}

		@Override
		public void startWriteAttr(RellocationMission node, XmlNodeAttributesWriter attr) throws IOException {
			attr.setId(node);
			attr.setPoint(DEST, node.rellocationDestination.x, node.rellocationDestination.y);
			
			attr.set(UNIT, node.unit);
			if (node.unitDestination != null) {
				attr.setPoint(UNIT_DEST, node.unitDestination.x, node.unitDestination.y);
			}
			
			if (node.carrier != null) {
				attr.set(CARRIER, node.carrier);
			}
			if (node.carrierDestination != null) {
				attr.setPoint(CARRIER_DEST, node.carrierDestination.x, node.carrierDestination.y);
			}
		}
		
		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "rellocationMission";
		}
	}
    
}