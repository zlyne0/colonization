package promitech.colonization.ai;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.Path;

class RellocationMission extends AbstractMission {
	final Tile rellocationDestination;
	
	final Unit unit;
	Tile unitDestination;
	
	Unit carrier;
	Tile carrierDestination;

	public RellocationMission(Tile rellocationDestination, Unit unit, Unit carrier) {
	    this.rellocationDestination = rellocationDestination;
	    this.unit = unit;
	    this.carrier = carrier;
	}

    public RellocationMission(Tile rellocationDestination, Unit unit) {
        this.rellocationDestination = rellocationDestination;
        this.unit = unit;
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
}