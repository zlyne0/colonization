package promitech.colonization.gamelogic.combat;

import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.Modifier;

class Combat {
	enum CombatResult {
		WIN, LOSE, EVADE_ATTACK;
	}
	private CombatResult combatResult = null;
	private boolean greatResult;
	private float offencePower;
	private float defencePower;
	private float winPropability;
	
	void init(Unit attacker, Unit defender) {
		combatResult = null;
		greatResult = false;
		
		offencePower = getOffencePower(attacker, defender);
		defencePower = getDefencePower(attacker, defender);

		winPropability = offencePower / (offencePower + defencePower);
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
	
	void doCombat(Unit attacker, Unit defender) {
		float offencePower = getOffencePower(attacker, defender);
		float defencePower = getDefencePower(attacker, defender);
		
		float victoryPropability = offencePower / (offencePower + defencePower);
		System.out.println("victory = " + victoryPropability);
	}
	
	private boolean isDefenderUnitBeached() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private boolean canDefenderEvadeAttack() {
		return false;
	}
	
	public float getOffencePower(Unit attacker, Unit defender) {
		ObjectWithFeatures mods = new ObjectWithFeatures("combat"); 
		
		mods.addModifier(new Modifier(
			Modifier.OFFENCE, 
			Modifier.ModifierType.ADDITIVE, 
			attacker.unitType.getBaseOffence()
		));
				
		mods.addModifierFrom(attacker.unitType, Modifier.OFFENCE);
		mods.addModifierFrom(attacker.unitRole, Modifier.OFFENCE);
    	
    	if (defender != null) {
    		// Special bonuses against certain nation types
    		mods.addModifierFrom(
				attacker.getOwner().nationType(), 
				Modifier.OFFENCE_AGAINST, 
				defender.getOwner().nationType()
			);
    	}

    	// Attack bonus
		mods.addModifier(
			Specification.instance.modifiers.getById(Modifier.ATTACK_BONUS)
		);
    	
    	if (attacker.isNaval()) {
    		addNavalOffenceModifiers(attacker, mods);
    	} else {
    		addLandOffenceModifiers(attacker, defender, mods);
    	}
    	
		return mods.applyModifiers(0);
	}
	
	public float getDefencePower(Unit attacker, Unit defender) {
		ObjectWithFeatures mods = new ObjectWithFeatures("defenceCombat");

		mods.addModifier(new Modifier(
			Modifier.DEFENCE, 
			Modifier.ModifierType.ADDITIVE, 
			defender.unitType.getBaseDefence()
		));
		
		mods.addModifierFrom(defender.unitType, Modifier.DEFENCE);
		mods.addModifierFrom(defender.unitRole, Modifier.DEFENCE);
		
        // Land/naval split
        if (defender.isNaval()) {
            addNavalDefensiveModifiers(defender, mods);
        } else {
            addLandDefensiveModifiers(attacker, defender, mods);
        }
        return mods.applyModifiers(0);
	}
	
	private void addNavalDefensiveModifiers(Unit defender, ObjectWithFeatures mods) {
		int cargo = defender.getGoodsContainer().getCargoSpaceTaken();
		if (cargo > 0) {
			Modifier cargoPenalty = new Modifier(
				Modifier.DEFENCE, 
				Modifier.ModifierType.PERCENTAGE,
				cargo * -12.5f 
			);
			mods.addModifier(cargoPenalty);
		}
	}
	
	private void addLandDefensiveModifiers(Unit attacker, Unit defender, ObjectWithFeatures mods) {
		Tile tile = defender.getTileLocationOrNull();
		mods.addModifierFrom(tile.getType(), Modifier.DEFENCE);
		
        if (defender.hasAbility(Ability.BOMBARD) && defender.getState() != Unit.UnitState.FORTIFIED) {
			mods.addModifier(
				Specification.instance.modifiers.getById(Modifier.ARTILLERY_IN_THE_OPEN)
			);
        }
	}
	
	private void addLandOffenceModifiers(Unit offenceUnit, Unit defenderUnit, ObjectWithFeatures mods) {
		Specification spec = Specification.instance;
		
		// Movement penalty
		switch (offenceUnit.getMovesLeft()) {
			case 1:
				mods.addModifier(
					spec.modifiers.getById(Modifier.BIG_MOVEMENT_PENALTY)
				);
				break;
			case 2:
				mods.addModifier(
					spec.modifiers.getById(Modifier.SMALL_MOVEMENT_PENALTY)
				);
				break;
			default: 
				break;
		}
		
		if (isCombatAmphibious(offenceUnit, defenderUnit)) {
			mods.addModifier(
				spec.modifiers.getById(Modifier.AMPHIBIOUS_ATTACK)
			);
		}
		
		if (isAmbush(offenceUnit, defenderUnit)) {
			Tile defenceTile = defenderUnit.getTileLocationOrNull();
			mods.addModifierFrom(defenceTile.getType(), Modifier.DEFENCE);
		}
		
		if (offenceUnit.hasAbility(Ability.BOMBARD)
				&& offenceUnit.getTileLocationOrNull() != null
				&& offenceUnit.getTileLocationOrNull().hasSettlement() == false
				&& offenceUnit.getState() != Unit.UnitState.FORTIFIED
				&& defenderUnit.getTileLocationOrNull() != null 
				&& defenderUnit.getTileLocationOrNull().hasSettlement() == false) {
			mods.addModifier(
				spec.modifiers.getById(Modifier.ARTILLERY_IN_THE_OPEN)
			);
		}
	}
	
	private void addNavalOffenceModifiers(Unit offenceUnit, ObjectWithFeatures mods) {
		int cargo = offenceUnit.getGoodsContainer().getCargoSpaceTaken();
		if (cargo > 0) {
			Modifier cargoPenalty = new Modifier(
				Modifier.OFFENCE, 
				Modifier.ModifierType.PERCENTAGE,
				cargo * -12.5f 
			);
			mods.addModifier(cargoPenalty);
		}
	}

	boolean isCombatAmphibious(Unit attacker, Unit defender) {
		return attacker.getTileLocationOrNull() == null && 
				defender.getTileLocationOrNull() != null;
	}

	boolean isAmbush(Unit attacker, Unit defender) {
		Tile attackerTile = attacker.getTileLocationOrNull();
		Tile defenderTile = defender.getTileLocationOrNull();
		return attackerTile != null && defenderTile != null  
			&& !attackerTile.hasSettlement() && !defenderTile.hasSettlement()
			&& (attacker.hasAbility(Ability.AMBUSH_BONUS) 
					|| defender.hasAbility(Ability.AMBUSH_PENALTY))
			&& (attackerTile.getType().hasAbility(Ability.AMBUSH_TERRAIN) 
					|| defenderTile.getType().hasAbility(Ability.AMBUSH_TERRAIN)); 
	}

	public float getOffencePower() {
		return offencePower;
	}

	public float getDefencePower() {
		return defencePower;
	}

	public float getWinPropability() {
		return winPropability;
	}
}
