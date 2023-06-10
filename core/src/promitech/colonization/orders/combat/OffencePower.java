package promitech.colonization.orders.combat;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;

import promitech.colonization.orders.diplomacy.NationSummary;

public class OffencePower {

	private static final Pool<CombatSides> combatSidesPool = Pools.get(CombatSides.class);

	public static NationSummary calculatePlayerUnitsOffencePower(Player player) {
		CombatSides combatSides = combatSidesPool.obtain();

		NationSummary nationSummary = new NationSummary();
		for (Unit unit : player.units) {
			if (unit.isNaval()) {
				nationSummary.navyPower += combatSides.getOffencePower(unit);
			} else {
				nationSummary.militaryPower += combatSides.getOffencePower(unit);
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

}
