package promitech.colonization.ai;

import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.Direction;

public class WanderMission implements Identifiable {

	final Unit unit;
	Direction previewDirection;
	
	public WanderMission(Unit unit) {
		this.unit = unit;
	}

	@Override
	public String getId() {
		return unit.getId();
	}

}
