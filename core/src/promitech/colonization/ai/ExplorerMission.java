package promitech.colonization.ai;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.Unit;

class ExplorerMission implements Identifiable {

	final Unit unit;
	
	public ExplorerMission(Unit unit) {
		this.unit = unit;
	}
	
	@Override
	public String getId() {
		return unit.getId();
	}

}
