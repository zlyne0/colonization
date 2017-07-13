package promitech.colonization.ai;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;

class FoundColonyMission extends AbstractMission {
	final Tile destTile;
	final Unit unit;
	
	public FoundColonyMission(Tile destTile, Unit unit) {
        this.destTile = destTile;
        this.unit = unit;
    }
	
	boolean isUnitInDestination() {
		Tile ut = unit.getTileLocationOrNull();
		return ut != null && destTile != null && ut.equalsCoordinates(destTile.x, destTile.y);
	}
	
	public String toString() {
		return "FoundColonyMission";
	}
}