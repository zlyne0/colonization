package promitech.colonization.orders.combat;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;

import promitech.colonization.orders.diplomacy.NationSummary;

public class OffencePower {

	private static final Pool<CombatSides> combatSidesPool = Pools.get(CombatSides.class);

	public static NationSummary calculatePlayerUnitsOffencePower(Player player) {
		CombatSides combatSides = combatSidesPool.obtain();

		NationSummary nationSummary = new NationSummary();
		for (Unit unit : player.units) {
			if (unit.isNaval()) {
				nationSummary.navyPower += combatSides.calculateOffencePower(unit);
			} else {
				nationSummary.militaryPower += combatSides.calculateOffencePower(unit);
			}
		}
		combatSidesPool.free(combatSides);
		return nationSummary;
	}

	public static float calculateTileDefencePowerForAttacker(Unit attacker, Tile tile) {
		CombatSides combatSides = combatSidesPool.obtain();
		combatSides.init(attacker, tile);
		float defencePower = combatSides.getDefencePower();
		combatSidesPool.free(combatSides);
		return defencePower;
	}

	public static float calculatePaperOffencePower(UnitType unitType, UnitRole unitRole) {
		CombatSides combatSides = combatSidesPool.obtain();
		float offencePower = combatSides.calculateOffencePower(unitType, unitRole);
		combatSidesPool.free(combatSides);
		return offencePower;
	}

	public static float calculatePaperDefencePower(UnitType unitType, UnitRole unitRole) {
		CombatSides combatSides = combatSidesPool.obtain();
		float offencePower = combatSides.calculateDefencePower(unitType, unitRole);
		combatSidesPool.free(combatSides);
		return offencePower;
	}

}
