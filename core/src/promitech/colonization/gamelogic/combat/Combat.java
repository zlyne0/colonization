package promitech.colonization.gamelogic.combat;

import java.util.ArrayList;
import java.util.Collection;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitLocation;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitRoleLogic;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Tension;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.RequiredGoods;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import promitech.colonization.Randomizer;

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

	CombatResult generateGreatLoss() {
    	return generateAttackResult(1f);
	}
	
	CombatResult generateGreatWin() {
    	return generateAttackResult(0f);
	}
	
	CombatResult generateOrdinaryWin() {
	    float r = combatSides.getWinPropability() - 0.001f;
	    return generateAttackResult(r);
	}
	
    CombatResult generateOrdinaryLoss() {
        float r = combatSides.getWinPropability() + (1f - combatSides.getWinPropability()) * 0.9f - 0.001f;
        return generateAttackResult(r);
    }
	
	CombatResult generateRandomResult() {
	    return generateAttackResult(Randomizer.instance().realProbability());
	}
	
	CombatResult generateAttackResult(float r) {
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
		return combatResult;
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
		// TODO:
		//combatResolver.resolve(greatResult, combatSides);
	}

	private void bombardCombat(float r, float winVal) {
		combatResolver.combatResultDetails.clear();
		
		if (r <= winVal) {
			combatResult = CombatResult.WIN;
		    // Great wins occur at most in 1 in 3 of successful bombards,
		    // Good defences reduce this proportion.
			float diff = Math.max(3f, combatSides.getDefencePower() * 2f - combatSides.getOffencePower());
			greatResult = r < winVal / diff;
			
			combatResolver.loser = combatSides.defender;
			if (greatResult || !combatSides.hasDefenderRepairLocation()) {
				combatResolver.combatResultDetails.add(CombatResultDetails.SINK_SHIP_BOMBARD);
			} else {
				combatResolver.combatResultDetails.add(CombatResultDetails.DAMAGE_SHIP_BOMBARD);
			}
		} else {
			combatResult = CombatResult.EVADE_ATTACK;
			combatResolver.combatResultDetails.add(CombatResultDetails.EVADE_BOMBARD);
		}
	}

	public void processAttackResult() {
        int winnerTension = 0;
        int loserTension = 0;
	    
	    System.out.println("combatResultDetails.size " + combatResolver.combatResultDetails.size());
		for (CombatResultDetails resultDetail : combatResolver.combatResultDetails) {
		    System.out.println(" - result " + resultDetail);
		    switch (resultDetail) {
		    case SINK_SHIP_ATTACK:
			case SINK_SHIP_BOMBARD: {
				Player loserPlayer = combatResolver.loser.getOwner();
				loserPlayer.removeUnit(combatResolver.loser);
			} break;
			case DAMAGE_SHIP_ATTACK:
			case DAMAGE_SHIP_BOMBARD:
				UnitLocation repairLocation = combatResolver.loser.getRepairLocation();
				combatResolver.loser.makeUnitDamaged(repairLocation);
				break;
			case LOOT_SHIP:
				combatResolver.loser.transferAllGoods(combatResolver.winner);
				break;
			case SLAUGHTER_UNIT: {
                Player loserPlayer = combatResolver.loser.getOwner();
                loserPlayer.removeUnit(combatResolver.loser);
                
                winnerTension -= Tension.TENSION_ADD_NORMAL;
                loserTension += getSlaughterTension(combatResolver.loser);
			} break;
			case PROMOTE_UNIT: combatResolver.winner.changeUnitType(ChangeType.PROMOTION);
			break;
			case CAPTURE_EQUIP: captureEquipment();
			break;
			case DEMOTE_UNIT: combatResolver.loser.changeUnitType(ChangeType.DEMOTION);
			break;
			case LOSE_EQUIP: combatResolver.loser.downgradeRole(); 
		    break;
			case CAPTURE_UNIT: combatResolver.winner.captureUnit(combatResolver.loser); 
			break;
			
			case DAMAGE_COLONY_SHIPS: damageColonyShips();
			break;
			
			case CAPTURE_COLONY: captureColony();
			break;
			case AUTOEQUIP_UNIT: // do nothing, in freecol its show message 
			break;
			case LOSE_AUTOEQUIP: loseAutoEquip();
			break;
			case CAPTURE_AUTOEQUIP: captureAutoEquip();
			break;
			
			case EVADE_BOMBARD: // do nothing
			default:
				break;
			}

			// TODO:
//        case AUTOEQUIP_UNIT:
//        case BURN_MISSIONS:
//        case CAPTURE_AUTOEQUIP:
//        case CAPTURE_COLONY:
//        case CAPTURE_CONVERT:
//        case DAMAGE_COLONY_SHIPS:
//        case DESTROY_COLONY:
//        case DESTROY_SETTLEMENT:
//        case EVADE_ATTACK:
//        case LOSE_AUTOEQUIP:
//        case PILLAGE_COLONY:
//        case SINK_COLONY_SHIPS:
		}
		
		// TODO: tension
	}

    private void loseAutoEquip() {
        UnitRole autoArmRole = combatSides.getDefenderAutoArmRole();
        Colony colony = combatSides.defenderTile.getSettlement().getColony();
        for (RequiredGoods requiredGoods : autoArmRole.requiredGoods.entities()) {
            colony.getGoodsContainer().decreaseGoodsQuantity(requiredGoods.goodsType.getId(), requiredGoods.amount);
        }
    }

	private void captureColony() {
		Colony colony = combatSides.defenderTile.getSettlement().getColony();
		Player winnerPlayer = combatResolver.winner.getOwner();
		Player losserPlayer = colony.getOwner();
		
		if (losserPlayer.hasGold()) {
		    int upper = (losserPlayer.getGold() * (colony.getUnits().size() + 1)) / (coloniesPopulation(losserPlayer) + 1);
		    if (upper > 0) {
		        int gold = Randomizer.instance().randomInt(1, upper);
		        if (gold > 0) {
		            losserPlayer.subtractGold(gold);
		            winnerPlayer.addGold(gold);
		        }
		    }
		}
		colony.changeOwner(winnerPlayer);
		
		colony.updateColonyFeatures();
		colony.updateColonyPopulation();
	}
	
	private int coloniesPopulation(Player player) {
	    int sum = 0;
	    for (Settlement settlement : player.settlements.entities()) {
	        sum += settlement.getUnits().size();
        }
	    return sum;
	}
	
	private void damageColonyShips() {
        boolean captureRepairing = Specification.options
    		.getBoolean(GameOptions.CAPTURE_UNITS_UNDER_REPAIR);
		
		UnitLocation repairLocation = combatResolver.loser.getRepairLocation();
		
		for (Unit unit : new ArrayList<Unit>(combatSides.defenderTile.getUnits().entities())) {
			if (unit.isNaval() && !(captureRepairing && unit.isDamaged())) {
				unit.makeUnitDamaged(repairLocation);
			}
		}
	}
	
    private int getSlaughterTension(Unit loser) {
        if (loser.getIndianSettlementId() != null) {
            return Tension.TENSION_ADD_UNIT_DESTROYED;
        } else {
            return Tension.TENSION_ADD_MINOR;
        }
    }

    private void captureAutoEquip() {
        UnitRole autoArmRole = combatSides.getDefenderAutoArmRole();
        captureEquipmentFromUserRole(autoArmRole);
    }

    private void captureEquipment() {
        combatResolver.loser.downgradeRole();
        
        // TODO: tutaj chyba powinno byc capture roznice rol 
        captureEquipmentFromUserRole(combatResolver.loser.unitRole);
    }
    
    private void captureEquipmentFromUserRole(UnitRole loserRole) {
        UnitRole newRole = combatResolver.winner.capturedEquipment(loserRole);
        if (newRole == null) {
            return;
        }
        if (combatResolver.winner.getOwner().isIndian()) {
            indianTransferCapturedGoodToHomeSettlement(newRole);
        }
        combatResolver.winner.changeRole(newRole);
    }

    private void indianTransferCapturedGoodToHomeSettlement(UnitRole roleEquipment) {
        // CHEAT: Immediately transferring the captured goods back to a potentially remote settlement
        // Apparently Col1 did it
        ProductionSummary equipment = UnitRoleLogic.requiredGoodsToChangeRole(combatResolver.winner, roleEquipment);
        if (combatResolver.winner.getIndianSettlementId() != null) {
            Settlement settlement = combatResolver.winner.getOwner().settlements.getByIdOrNull(combatResolver.winner.getIndianSettlementId());
            if (settlement != null) {
                for (Entry<String> goods : equipment.entries()) {
                    settlement.addGoods(goods.key, goods.value);
                }
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
