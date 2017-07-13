package promitech.colonization.ai;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;

class TransportUnitMission extends AbstractMission {
	Unit carrier;
	Tile dest;
	final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
	
	public String toString() {
		return "TransportUnitMission";
	}
}