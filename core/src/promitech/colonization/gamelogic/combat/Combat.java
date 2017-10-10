package promitech.colonization.gamelogic.combat;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.Scope;

class Combat {
	
	enum CombatResult {
		WIN, LOSE, EVADE_ATTACK;
	}
	private CombatResult combatResult = null;
	private boolean greatResult;
	private final CombatSides combatSides = new CombatSides();
	
	public void init(Colony colony, Tile defenderTile, Unit defender) {
		combatResult = null;
		greatResult = false;
		combatSides.init(colony, defenderTile, defender);
	}
	
	public void init(Unit attacker, Tile tile) {
		combatResult = null;
		greatResult = false;
		combatSides.init(attacker, tile);
	}
	
	void generateAttackResult(float r, float winVal) {
		if (isAttack()) {
			if (r <= winVal || isDefenderUnitBeached()) {
				combatResult = CombatResult.WIN;
				greatResult = r < 0.1f * winVal; // Great Win
			} else {
				if (r < 0.8f * winVal + 0.2f && canDefenderEvadeAttack()) {
					combatResult = CombatResult.EVADE_ATTACK;
				} else {
					combatResult = CombatResult.LOSE;
					greatResult = r >= 0.1f * winVal + 0.9f; // Great Loss
				}
			}
		} else {
			if (isBombard()) {
				if (r <= winVal) {
					combatResult = CombatResult.WIN;
				}
			} else {
				throw new IllegalStateException("no attack and no bombard combat");
			}
		}
		if (combatResult == null) {
			throw new IllegalStateException("no attack result");
		}
	}

	private boolean isAttack() {
		return false;
	}
	
	private boolean isBombard() {
		return false;
	}
	
//	void doCombat(Unit attacker, Unit defender) {
//		float offencePower = getOffencePower(attacker, defender);
//		float defencePower = getDefencePower(defender);
//		
//		float victoryPropability = offencePower / (offencePower + defencePower);
//		System.out.println("victory = " + victoryPropability);
//	}
	
	private boolean isDefenderUnitBeached() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private boolean canDefenderEvadeAttack() {
		// TODO:
		return false;
	}

	public float getOffencePower() {
		return combatSides.getOffencePower();
	}

	public float getDefencePower() {
		return combatSides.getDefencePower();
	}

	public float getWinPropability() {
		return combatSides.getWinPropability();
	}
	
	public String toString() {
		return "offence: " + combatSides.getOffencePower() + 
				", defence: " + combatSides.getDefencePower() + 
				", winPropability: " + combatSides.getWinPropability();
	}
}
