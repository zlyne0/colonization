package promitech.colonization.orders.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ObjectWithFeatures;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.player.Stance;
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
	protected Unit winner;
	protected Unit loser;
	protected Colony bombardmentColony;
	protected boolean combatAmphibious;
	private float offencePower;
	private float defencePower;
	private float winPropability;
	private final List<Ability> automaticEquipmentAbilities = new ArrayList<Ability>();
	private UnitRole defenderAutoArmRole;

	protected final ObjectWithFeatures offenceModifers = new ObjectWithFeatures("combat"); 
	protected final ObjectWithFeatures defenceModifiers = new ObjectWithFeatures("defenceCombat");

	void init(Unit attacker, Tile tile) {
		this.winner = null;
		this.loser = null;
		this.bombardmentColony = null;
		this.defenderTile = tile;
		Unit defender = getTileDefender(attacker, tile);
		
		this.defenderAutoArmRole = null;
		this.attacker = attacker;
		this.defender = defender;
		this.combatType = CombatType.ATTACK;
		this.combatAmphibious = this.isCombatAmphibious(attacker, defender);
		offencePower = getOffencePower(attacker, defender);
		defencePower = getDefencePower(attacker, defender, defenderTile);

		winPropability = offencePower / (offencePower + defencePower);
	}
	
	void init(Colony colony, Tile defenderTile, Unit defender) {
		this.winner = null;
		this.loser = null;
		this.defenderTile = defenderTile;
		if (defender == null || !defender.isNaval()) {
			throw new IllegalStateException("no defender to bombard or it is not naval");
		}
		if (!colony.colonyUpdatableFeatures.hasAbility(Ability.BOMBARD_SHIPS)) {
			throw new IllegalStateException("colony " + colony.getId() + " has no bombard ship ability");
		}
		this.bombardmentColony = colony;
		this.defenderAutoArmRole = null;
		this.attacker = null;
		this.defender = defender;
		this.combatType = CombatType.BOMBARD;
		this.combatAmphibious = false;
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
	
	// msg keys
	private static final String BASE_OFFENCE = "model.source.baseOffence";
	private static final String BASE_DEFENCE = "model.source.baseDefence";
	private static final String CARGO_PENALTY = "model.source.cargoPenalty";
	private static final String MOVEMENT_PENALTY = "model.source.movementPenalty";
	private static final String ATTACK_BONUS = "model.source.attackBonus";
	private static final String AMPHIBIOUS_ATTACK_PENALTY = "model.source.amphibiousAttack";
	private static final String ARTILLERY_PENALTY = "model.source.artilleryInTheOpen";
	private static final String FORTIFICATION_BONUS = "model.source.fortified";
	private static final String INDIAN_RAID_BONUS = "model.source.artilleryAgainstRaid";
	
	public final java.util.Map<String, String> combatModifiersNames = new HashMap<String, String>();
	{
		combatModifiersNames.put(Modifier.OFFENCE, BASE_OFFENCE);
		combatModifiersNames.put(Modifier.DEFENCE, BASE_DEFENCE);
		combatModifiersNames.put(Modifier.COMBAT_CARGO_PENALTY, CARGO_PENALTY);
		combatModifiersNames.put(Modifier.SMALL_MOVEMENT_PENALTY, MOVEMENT_PENALTY);
		combatModifiersNames.put(Modifier.BIG_MOVEMENT_PENALTY, MOVEMENT_PENALTY);
		combatModifiersNames.put(Modifier.ARTILLERY_IN_THE_OPEN, ARTILLERY_PENALTY);
		combatModifiersNames.put(Modifier.ATTACK_BONUS, ATTACK_BONUS);
		combatModifiersNames.put(Modifier.FORTIFIED, FORTIFICATION_BONUS);
		combatModifiersNames.put(Modifier.ARTILLERY_AGAINST_RAID, INDIAN_RAID_BONUS);
		combatModifiersNames.put(Modifier.AMPHIBIOUS_ATTACK, AMPHIBIOUS_ATTACK_PENALTY);
	}
	
	public float getOffencePower(Unit attacker, Unit defender) {
		offenceModifers.clearLists();
		
		if (attacker.unitType.getBaseOffence() != 0) {
    		offenceModifers.addModifier(new Modifier(
    			Modifier.OFFENCE, 
    			Modifier.ModifierType.ADDITIVE, 
    			attacker.unitType.getBaseOffence()
    		));
		}
				
		offenceModifers.addModifierFrom(attacker.unitType, Modifier.OFFENCE);
		offenceModifers.addModifierFrom(
			attacker.getOwner().getFeatures(), 
			Modifier.OFFENCE, 
			attacker.unitType
		);
		offenceModifers.addModifierFrom(attacker.unitRole, Modifier.OFFENCE);
    	
    	if (defender != null) {
    		// Special bonuses against certain nation types
    		offenceModifers.addModifierFrom(
				attacker.getOwner().nationType(), 
				Modifier.OFFENCE_AGAINST, 
				defender.getOwner().nationType()
			);
    	}

    	// Attack bonus
		offenceModifers.addModifier(
			Specification.instance.modifiers.getById(Modifier.ATTACK_BONUS)
		);
    	
    	if (attacker.isNaval()) {
    		addNavalOffenceModifiers(attacker, offenceModifers);
    	} else {
    		addLandOffenceModifiers(attacker, defender, offenceModifers);
    	}
    	
		return offenceModifers.applyModifiers(0);
	}
	
	private float getDefencePower(Unit attacker, Unit defender, Tile tileDefender) {
		defenceModifiers.clearLists();

		defenceModifiers.addModifier(new Modifier(
			Modifier.DEFENCE, 
			Modifier.ModifierType.ADDITIVE, 
			defender.unitType.getBaseDefence()
		));
		
		defenceModifiers.addModifierFrom(defender.unitType, Modifier.DEFENCE);
		defenceModifiers.addModifierFrom(
			defender.getOwner().getFeatures(), 
			Modifier.DEFENCE, 
			defender.unitType
		);
		defenceModifiers.addModifierFrom(defender.unitRole, Modifier.DEFENCE);
		
        // Land/naval split
        if (defender.isNaval()) {
            addNavalDefensiveModifiers(defender, defenceModifiers);
        } else {
            addLandDefensiveModifiers(attacker, defender, defenceModifiers, tileDefender);
        }
        return defenceModifiers.applyModifiers(0);
	}

	private void addNavalDefensiveModifiers(Unit defender, ObjectWithFeatures mods) {
		int cargo = defender.getGoodsContainer().getCargoSpaceTaken();
		if (cargo > 0) {
			Modifier cargoPenalty = new Modifier(
				Modifier.COMBAT_CARGO_PENALTY, 
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
			
			if (!defender.hasAbility(Ability.BOMBARD)) {
			    addSettlementAutoArmDefensiveModifiers(mods, defender, settlement);
			}
			
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
					UnitRole tmpAutoArmRole = Specification.instance.unitRoles.getById(scope.getType());
					if (settlement.hasGoodsToEquipRole(tmpAutoArmRole)) {
					    this.defenderAutoArmRole = tmpAutoArmRole;
						mods.addModifierFrom(tmpAutoArmRole, Modifier.DEFENCE);
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
		
		if (combatAmphibious) {
			mods.addModifier(
				spec.modifiers.getById(Modifier.AMPHIBIOUS_ATTACK)
			);
		}
		
		// Ambush bonus in the open = defender's defence
		// bonus, if defender is REF, or attacker is indian.
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
				Modifier.COMBAT_CARGO_PENALTY, 
				Modifier.ModifierType.PERCENTAGE,
				cargo * -12.5f
			);
			mods.addModifier(cargoPenalty);
		}
	}

	private boolean isCombatAmphibious(Unit attacker, Unit defender) {
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
		    if (!properDefenderForTile(tile, u)) {
		        continue;
		    }
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

	private boolean properDefenderForTile(Tile tile, Unit unit) {
	    return unit.isNaval() == tile.getType().isWater();
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

	public Stance stance() {
		return attacker.getOwner().getStance(defender.getOwner());
	}

	public boolean hasDefenderAutoArmRole() {
	    return defenderAutoArmRole != null;
	}
	
    public UnitRole getDefenderAutoArmRole() {
        return defenderAutoArmRole;
    }

	protected boolean isDefenderLoser() {
	    return loser.equalsId(defender);
	}
	
	protected boolean isAttackerWon() {
	    return attacker.equalsId(winner);	    
	}
	
	protected boolean isAttackerLose() {
		return attacker.equalsId(loser);
	}
    
}
