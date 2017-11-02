package promitech.colonization.gamelogic.combat;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Stance;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.ui.resources.StringTemplate;

class Combat {
	
	enum CombatResult {
		WIN, LOSE, EVADE_ATTACK;
	}
	
	enum CombatResultDetails {
        AUTOEQUIP_UNIT,       // Defending unit auto-arms
        BURN_MISSIONS,        // Defending natives burn attackers missions
        CAPTURE_AUTOEQUIP,    // Winner captures loser auto-equipment
        CAPTURE_COLONY,       // Winning Europeans capture a colony
        CAPTURE_CONVERT,      // Winning Europeans cause native to convert
        CAPTURE_EQUIP,        // Winner captures loser equipment
        CAPTURE_UNIT,         // Losing unit is captured
        DAMAGE_COLONY_SHIPS,  // Ships in losing colony are damaged
        DAMAGE_SHIP_ATTACK,   // Losing ship is damaged by normal attack
        DAMAGE_SHIP_BOMBARD,  // Losing ship is damaged by bombardment
        DEMOTE_UNIT,          // Losing unit is demoted
        DESTROY_COLONY,       // Winning natives burn a colony
        DESTROY_SETTLEMENT,   // Winner destroys a native settlement
        EVADE_ATTACK,         // Defending ship evades normal attack
        EVADE_BOMBARD,        // Defending ship evades bombardment
        LOOT_SHIP,            // Losing ship is looted
        LOSE_AUTOEQUIP,       // Losing unit auto-arms and loses the arms
        LOSE_EQUIP,           // Losing unit loses some equipment
        PILLAGE_COLONY,       // Winning natives pillage an undefended colony
        PROMOTE_UNIT,         // Winning unit is promoted
        SINK_COLONY_SHIPS,    // Ships in losing colony are sunk
        SINK_SHIP_ATTACK,     // Losing ship is sunk by normal attack
        SINK_SHIP_BOMBARD,    // Losing ship is sunk by bombardment
        SLAUGHTER_UNIT,       // Losing unit is slaughtered
	}
	
	protected CombatResult combatResult = null;
	protected boolean greatResult;
	protected final CombatSides combatSides = new CombatSides();
	protected final CombatResolver combatResolver = new CombatResolver();
	
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
	
	void generateAttackResult(float r) {
		switch (combatSides.getCombatType()) {
			case ATTACK: 
				attackCombat(r, combatSides.getWinPropability());
				break;
			case BOMBARD:
				bombardCombat(r, combatSides.getWinPropability());
				break;
			default:
				throw new IllegalStateException("no attack and no bombard combat");
		}
		
		if (combatResult == null) {
			throw new IllegalStateException("attack result not generated");
		}
	}

	private void attackCombat(float r, float winVal) {
		// For random float 0 <= r < 1.0:
		// Partition this range into wins < odds.win and losses above.
		// Within the 0 <= r < odds.win range, partition the first 10%
		// to be great wins and the rest to be ordinary wins.
		//   r < 0.1 * odds.win  => great win
		//   else r < odds.win   => win
		// Within the odds.win <= r < 1.0 range, partition the first
		// 20% to be evasions (if defender has the evadeAttack ability),
		// the next 70% to be ordinary losses, and the rest great losses.
		//   r < odds.win + 0.2 * (1.0 - odds.win) = 0.8 * odds.win + 0.2
		//     => evade
		//   else r < odds.win + (0.2 + 0.7) * (1.0 - odds.win)
		//     = 0.1 * odds.win + 0.9 => loss
		//   else => great loss
		// ...and beached ships always lose.
		
		if (r <= winVal || combatSides.isDefenderUnitBeached()) {
			combatResult = CombatResult.WIN;
			greatResult = r < 0.1f * winVal; // Great Win
			combatResolver.init(combatSides.attacker, combatSides.defender, greatResult, combatSides);
		} else {
			if (r < 0.8f * winVal + 0.2f && combatSides.canDefenderEvadeAttack()) {
				combatResult = CombatResult.EVADE_ATTACK;
				combatResolver.initNoResult();
				// TODO: no resolvAttack
			} else {
				combatResult = CombatResult.LOSE;
				greatResult = r >= 0.1f * winVal + 0.9f; // Great Loss
				combatResolver.init(combatSides.defender, combatSides.attacker, greatResult, combatSides);
			}
		}
		//combatResolver.resolve(greatResult, combatSides);
	}

	private void bombardCombat(float r, float winVal) {
		if (r <= winVal) {
			combatResult = CombatResult.WIN;
			// TODO: trzeba wydzielic w combat result takie tam atrybuty
			
		    // Great wins occur at most in 1 in 3 of successful bombards,
		    // Good defences reduce this proportion.
			float diff = Math.max(3f, combatSides.getDefencePower() * 2f - combatSides.getOffencePower());
			greatResult = r < winVal / diff;
			
			if (greatResult || !combatSides.hasDefenderRepairLocation()) {
				// crs.add(CombatResult.SINK_SHIP_BOMBARD);
			} else {
				// crs.add(CombatResult.DAMAGE_SHIP_BOMBARD);
			}
			
			// TODO: resolvAttack
		} else {
			combatResult = CombatResult.EVADE_ATTACK;
			// TODO: no resolvAttack
		}
	}

	public void processAttackResult() {
		for (CombatResultDetails resultDetail : combatResolver.combatResultDetails) {
			
			if (CombatResultDetails.SINK_SHIP_ATTACK == resultDetail) {
				Player loserPlayer = combatResolver.loser.getOwner();
				loserPlayer.removeUnit(combatResolver.loser);
			}
			
			if (CombatResultDetails.LOOT_SHIP == resultDetail) {
				combatResolver.loser.transferAllGoods(combatResolver.winner);
			}
		}
	}

	public boolean canAttackWithoutConfirmation() {
		if (combatSides.attacker.hasAbility(Ability.PIRACY)) {
			return true;
		}
        if (combatSides.defender.hasAbility(Ability.PIRACY)) {
            return true;
        }
        
        if (combatSides.attacker.getOwner().atWarWith(combatSides.defender.getOwner())) {
        	return true;
        }
        return false;
	}
	
	public StringTemplate attackConfirmationMessageTemplate() {
		Stance stance = combatSides.attacker.getOwner().getStance(combatSides.defender.getOwner());
		String msgStr = null;
		switch (stance) {
	        case WAR:
	        	throw new IllegalStateException("Player at war, no confirmation needed");
	        case CEASE_FIRE:
	        	msgStr = "model.diplomacy.attack.ceaseFire";
	        	break;
	        case ALLIANCE:
	        	msgStr = "model.diplomacy.attack.alliance";
	        	break;
	        case UNCONTACTED: 
	    	case PEACE: 
			default:
				msgStr = "model.diplomacy.attack.peace";
				break;
		}
		
		return StringTemplate.template(msgStr)
			.addStringTemplate("%nation%", combatSides.defender.getOwner().getNationName());
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
