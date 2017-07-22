package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.AbstractMission;
import net.sf.freecol.common.model.ai.UnitMissionsMapping;

public class TransportUnitMission extends AbstractMission {
	Unit carrier;
	Tile dest;
	final MapIdEntities<Unit> units = new MapIdEntities<Unit>();
	
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
}