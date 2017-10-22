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

class CombatSides {

	/**
     * The maximum attack power of a Colony's fortifications against a
     * naval unit.
     */
    public static final int MAXIMUM_BOMBARD_POWER = 48;
	
    /** A defence percentage bonus that disables the fortification bonus. */
	private static final int STRONG_DEFENCE_THRESHOLD = 150; // percent
	private final Modifier.ModifierPredicate hasStrongDefenceModifierPredicate = new Modifier.ModifierPredicate() {
		@Override
		public boolean apply(Modifier modifier) {
			return modifier.isPercentageType() && modifier.getValue() >= STRONG_DEFENCE_THRESHOLD;
		}
	};
	
	private CombatType combatType = null;
	protected Unit attacker;
	protected Unit defender;
	protected Tile defenderTile;
	private float offencePower;
	private float defencePower;
	private float winPropability;
	private final List<Ability> automaticEquipmentAbilities = new ArrayList<Ability>();

	void init(Unit attacker, Tile tile) {
		this.defenderTile = tile;
		Unit defender = getTileDefender(attacker, tile);
		
		this.attacker = attacker;
		this.defender = defender;
		this.combatType = CombatType.ATTACK;
		offencePower = getOffencePower(attacker, defender);
		defencePower = getDefencePower(attacker, defender, defenderTile);

		winPropability = offencePower / (offencePower + defencePower);
	}
	
	void init(Colony colony, Tile defenderTile, Unit defender) {
		this.defenderTile = defenderTile;
		if (defender == null || !defender.isNaval()) {
			throw new IllegalStateException("no defender to bombard or it is not naval");
		}
		if (!colony.colonyUpdatableFeatures.hasAbility(Ability.BOMBARD_SHIPS)) {
			throw new IllegalStateException("colony " + colony.getId() + " has no bombard ship ability");
		}
		
		this.attacker = null;
		this.defender = defender;
		this.combatType = CombatType.BOMBARD;
		offencePower = colonyOffenceBombardPower(colony);
		defencePower = getDefencePower(null, defender, defenderTile);
		winPropability = offencePower / (offencePower + defencePower);
	}
	

	private float colonyOffenceBombardPower(Colony colony) {
		float power = 0;
		
		for (Unit u : colony.tile.getUnits().entities()) {
			if (u.hasAbility(Ability.BOMBARD)) {
				power += u.unitType.applyModifier(Modifier.OFFENCE, u.unitType.getBaseOffence());
			}
		}
		if (power > MAXIMUM_BOMBARD_POWER) {
			power = MAXIMUM_BOMBARD_POWER;
		}
		return power;
	}
	
	public float getOffencePower(Unit attacker, Unit defender) {
		ObjectWithFeatures mods = new ObjectWithFeatures("combat"); 
		
		mods.addModifier(new Modifier(
			Modifier.OFFENCE, 
			Modifier.ModifierType.ADDITIVE, 
			attacker.unitType.getBaseOffence()
		));
				
		mods.addModifierFrom(attacker.unitType, Modifier.OFFENCE);
		mods.addModifierFrom(
			attacker.getOwner().getFeatures(), 
			Modifier.OFFENCE, 
			attacker.unitType
		);
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
	
	private float getDefencePower(Unit attacker, Unit defender, Tile tileDefender) {
		ObjectWithFeatures mods = new ObjectWithFeatures("defenceCombat");

		mods.addModifier(new Modifier(
			Modifier.DEFENCE, 
			Modifier.ModifierType.ADDITIVE, 
			defender.unitType.getBaseDefence()
		));
		
		mods.addModifierFrom(defender.unitType, Modifier.DEFENCE);
		mods.addModifierFrom(
			defender.getOwner().getFeatures(), 
			Modifier.DEFENCE, 
			defender.unitType
		);
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

	public float getOffencePower() {
		return offencePower;
	}

	public float getDefencePower() {
		return defencePower;
	}

	public float getWinPropability() {
		return winPropability;
	}

	public CombatType getCombatType() {
		return combatType;
	}
	
	public boolean hasDefenderRepairLocation() {
		return defender.hasRepairLocation();
	}
	
	public boolean canDefenderEvadeAttack() {
		return defender.hasAbility(Ability.EVADE_ATTACK);
	}
	
	public boolean isDefenderUnitBeached() {
		return defender.isBeached();
	}
	
}
