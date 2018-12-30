package promitech.colonization.orders.combat;

import net.sf.freecol.common.model.Unit;

public class OffencePower {

	private CombatSides combatSides = new CombatSides();
	
	public float calculateUnitOffencePower(Unit unit) {
		return combatSides.getOffencePower(unit);
	}
	
}
