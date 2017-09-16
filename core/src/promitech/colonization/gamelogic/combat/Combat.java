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
	
    /** A defence percentage bonus that disables the fortification bonus. */
	private static final int STRONG_DEFENCE_THRESHOLD = 150; // percent
	private final Modifier.ModifierPredicate hasStrongDefenceModifierPredicate = new Modifier.ModifierPredicate() {
		@Override
		public boolean apply(Modifier modifier) {
			return modifier.isPercentageType() && modifier.getValue() >= STRONG_DEFENCE_THRESHOLD;
		}
	};
    
	enum CombatResult {
		WIN, LOSE, EVADE_ATTACK;
	}
	private CombatResult combatResult = null;
	private boolean greatResult;
	private float offencePower;
	private float defencePower;
	private float winPropability;

	private final List<Ability> automaticEquipmentAbilities = new ArrayList<Ability>();
	
	private void init(Unit attacker, Unit defender, Tile defenderTile) {
		combatResult = null;
		greatResult = false;
		
		offencePower = getOffencePower(attacker, defender);
		defencePower = getDefencePower(attacker, defender, defenderTile);

		winPropability = offencePower / (offencePower + defencePower);
	}
	
	public void init(Colony colony, Tile tile) {
		
	}
	
	public void init(Unit attacker, Tile tile) {
		Unit defender = getTileDefender(attacker, tile);
		init(attacker, defender, tile);
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
	
	Unit getTileDefender(Unit attacker, Tile tile) {
		Unit defender = null;
		float defenderPower = 0;
		
		for (Unit u : tile.getUnits().entities()) {
			float p = getDefencePower(attacker, u, tile);
			if (betterDefender(defender, defenderPower, u, p)) {
				defenderPower = p;
				defender = u;
			}
		}
		if (defender == null && tile.hasSettlement()) {
			for (Unit u : tile.getSettlement().getUnits().entities()) {
				float p = getDefencePower(attacker, u, tile);
				if (betterDefender(defender, defenderPower, u, p)) {
					defenderPower = p;
					defender = u;
				}
			}
			if (defender == null) {
				throw new IllegalStateException(
					"no worker in settlement: " + tile.getSettlement().getId() + 
					", tile: " + tile.getId()
				);
			}
		}		
		if (defender == null) {
			throw new IllegalStateException("no defender on tile " + tile);
		}
		return defender;
	}
	
    private boolean betterDefender(
		Unit defender, float defenderPower,
        Unit other, float otherPower) 
    {
    	if (defender == null) {
    		return true;
    	} else if (defender.isPerson() && other.isPerson() && !defender.isArmed() && other.isArmed()) {
            return true;
        } else if (defender.isPerson() && other.isPerson() && defender.isArmed() && !other.isArmed()) {
            return false;
        } else if (!defender.isDefensiveUnit() && other.isDefensiveUnit()) {
            return true;
        } else if (defender.isDefensiveUnit() && !other.isDefensiveUnit()) {
            return false;
        } else {
            return defenderPower < otherPower;
        }
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
	
	public float getDefencePower(Unit attacker, Unit defender, Tile tileDefender) {
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
            addLandDefensiveModifiers(attacker, defender, mods, tileDefender);
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
	
	private void addLandDefensiveModifiers(Unit attacker, Unit defender, ObjectWithFeatures mods, Tile defenderTile) {
		boolean disableFortified = false;
		
		mods.addModifierFrom(defenderTile.getType(), Modifier.DEFENCE);
		disableFortified |= defenderTile.getType().hasModifier(Modifier.DEFENCE, hasStrongDefenceModifierPredicate);
		
		if (defenderTile.hasSettlement()) {
			Settlement settlement = defenderTile.getSettlement();
			settlement.addModifiersTo(mods, Modifier.DEFENCE);
			
			// Artillery defence bonus against an Indian raid
			if (defender.hasAbility(Ability.BOMBARD) 
				&& attacker != null 
				&& attacker.getOwner().isIndian()) 
			{
				mods.addModifier(Specification.instance.modifiers.getById(Modifier.ARTILLERY_AGAINST_RAID));
			}
			
			addSettlementAutoArmDefensiveModifiers(mods, defender, settlement);
			
			if (settlement.isColony()) {
				disableFortified |= hasBuildingWithStrongDefence(settlement.getColony());
			}
		} else {
			if (defender.hasAbility(Ability.BOMBARD) && defender.getState() != Unit.UnitState.FORTIFIED) {
				mods.addModifier(
					Specification.instance.modifiers.getById(Modifier.ARTILLERY_IN_THE_OPEN)
				);
			}
		}
		
		if (Unit.UnitState.FORTIFIED.equals(defender.getState()) && !disableFortified) {
			mods.addModifier(
				Specification.instance.modifiers.getById(Modifier.FORTIFIED)
			);
		}
	}
	
	private boolean hasBuildingWithStrongDefence(Colony colony) {
		for (Building b : colony.buildings.entities()) {
			if (b.buildingType.hasModifier(Modifier.DEFENCE)) {
				return b.buildingType.hasModifier(Modifier.DEFENCE, hasStrongDefenceModifierPredicate);
			}
		}
		return false;
	}
	
	/**
	 * Paul Revere makes an unarmed colonist in a settlement pick up a
	 * stock-piled musket if attacked, so the bonus should be applied for
	 * unarmed colonists inside colonies where there are muskets available.
	 * Natives can also auto-arm.
	 * @param mods 
	 */
	private void addSettlementAutoArmDefensiveModifiers(ObjectWithFeatures mods, Unit defender, Settlement settlement) {
		// unarm unit
		if (!defender.unitRole.isDefaultRole()) {
			return;
		}
		
		automaticEquipmentAbilities.clear();
		defender.getAbilities(Ability.AUTOMATIC_EQUIPMENT, automaticEquipmentAbilities);
		
		if (!automaticEquipmentAbilities.isEmpty()) {
			for (Ability a : automaticEquipmentAbilities) {
				for (Scope scope : a.getScopes()) {
					UnitRole autoArmRole = Specification.instance.unitRoles.getById(scope.getType());
					if (settlement.hasGoodsToEquipRole(autoArmRole)) {
						mods.addModifierFrom(autoArmRole, Modifier.DEFENCE);
						return;
					}
				}
			}
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
	
	public String toString() {
		return "offence: " + offencePower + ", defence: " + defencePower + ", winPropability: " + winPropability;
	}
}
