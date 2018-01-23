package promitech.colonization.gamelogic.combat;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.UnitTypeChange;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import promitech.colonization.Randomizer;
import promitech.colonization.gamelogic.combat.Combat.CombatResultDetails;

class CombatResolver {

	protected Unit winner; 
	protected Unit loser;
	protected List<CombatResultDetails> combatResultDetails = new ArrayList<Combat.CombatResultDetails>(5);
	private boolean greatResult;
	private boolean loserMustDie;
	
	public void init(Unit winner, Unit loser, boolean greatResult, CombatSides combatSides) {
		combatResultDetails.clear();
		this.winner = winner;
		this.loser = loser;
		this.greatResult = greatResult;
		
		resolve(combatSides);
	}

	public void initNoResult() {
		combatResultDetails.clear();
	}
	
	private void resolve(CombatSides combatSides) {
		loserMustDie = loser.hasAbility(Ability.DISPOSE_ON_COMBAT_LOSS);
		
		if (loser.isNaval()) {
			if (winner.isNaval() 
					&& winner.hasAbility(Ability.CAPTURE_GOODS) 
					&& loser.hasGoodsCargo()) {
				combatResultDetails.add(CombatResultDetails.LOOT_SHIP);
			}
			if (greatResult || loserMustDie || !loser.hasRepairLocation() || loser.isBeached()) {
				combatResultDetails.add(CombatResultDetails.SINK_SHIP_ATTACK);
			} else {
				combatResultDetails.add(CombatResultDetails.DAMAGE_SHIP_ATTACK);
			}
		} else {
			Tile defenderTile = combatSides.defenderTile;
			if (defenderTile.hasSettlement()) {
				if (defenderTile.getSettlement().isColony()) {
					colonyCombatResultDetails(combatSides);
				} else {
					indianSettlementCombatResultDetails();
				}
			} else {
				landCombatResultDetails(combatSides);
			}
		}
		unitPromotion(greatResult);
	}

	private boolean isDefenderLoser(CombatSides combatSides) {
	    return loser.equalsId(combatSides.defender);
	}
	
	private void landCombatResultDetails(CombatSides combatSides) {
	    if (isDefenderLoser(combatSides) && combatSides.hasDefenderAutoArmRole()) {
	        
	        if (winner.canCaptureEquipment(combatSides.getDefenderAutoArmRole())) {
	            combatResultDetails.add(CombatResultDetails.CAPTURE_AUTOEQUIP);
	            combatResultDetails.add(CombatResultDetails.LOSE_AUTOEQUIP);
	        } else {
	            combatResultDetails.add(CombatResultDetails.LOSE_AUTOEQUIP);
	        }
	        if (loserMustDie) {
	            combatResultDetails.add(CombatResultDetails.SLAUGHTER_UNIT);
	        } else if (loser.hasAbility(Ability.DEMOTE_ON_ALL_EQUIPMENT_LOST)) {
	            combatResultDetails.add(CombatResultDetails.DEMOTE_UNIT);
	        }
	        
	    } else if (loserMustDie) {
			combatResultDetails.add(CombatResultDetails.SLAUGHTER_UNIT);
		} else if (loser.unitRole.isOffensive()) {
			if (winner.canCaptureEquipment(loser)) {
				combatResultDetails.add(CombatResultDetails.CAPTURE_EQUIP);
			} else {
				combatResultDetails.add(CombatResultDetails.LOSE_EQUIP);
			}
			
			if (loser.losingEquipmentKillsUnit()) {
				combatResultDetails.add(CombatResultDetails.SLAUGHTER_UNIT);
			} else if (loser.losingEquipmentDemotesUnit()) {
				combatResultDetails.add(CombatResultDetails.DEMOTE_UNIT);
			}
			
		// But some can be captured.
		} else if (loser.hasAbility(Ability.CAN_BE_CAPTURED) 
		        && winner.hasAbility(Ability.CAPTURE_UNITS) 
		        && !combatSides.combatAmphibious) {
			combatResultDetails.add(CombatResultDetails.CAPTURE_UNIT);
			
		// Or losing just causes a demotion.
		} else if (loser.canUpgradeByChangeType(ChangeType.DEMOTION)) {
				combatResultDetails.add(CombatResultDetails.DEMOTE_UNIT);
		} else {
			// default
			combatResultDetails.add(CombatResultDetails.SLAUGHTER_UNIT);
		}
	}

	private void unitPromotion(boolean greatResult) {
		UnitTypeChange promotion = winner.unitType.getUnitTypeChange(ChangeType.PROMOTION, winner.getOwner());
		if (promotion == null) {
			return;
		}
		if (winner.hasAbility(Ability.AUTOMATIC_PROMOTION)) {
			combatResultDetails.add(CombatResultDetails.PROMOTE_UNIT);
			return;
		}
		if (!greatResult) {
			return;
		}
		if (Randomizer.instance().isHappen(promotion.getProbability(ChangeType.PROMOTION))) {
			combatResultDetails.add(CombatResultDetails.PROMOTE_UNIT);
		}
	}
	
	// A Colony falls to Europeans when the last defender
	// is unarmed.  Natives will pillage if possible but
	// otherwise proceed to kill colonists incrementally
	// until the colony falls for lack of survivors.
	// Ships in a falling colony will be damaged or sunk
	// if they have no repair location.
	private void colonyCombatResultDetails(CombatSides combatSides) {
	    // TODO: sometime after attack move unit:  moveAttacker = true;
	    if (loser.isDefensiveUnit() || isDefenderLoser(combatSides) && combatSides.hasDefenderAutoArmRole()) {
	        if (isDefenderLoser(combatSides) && combatSides.hasDefenderAutoArmRole()) {
	            combatResultDetails.add(CombatResultDetails.AUTOEQUIP_UNIT);
	        }
	        landCombatResultDetails(combatSides);
	        return;
	    }
	    
	    if (winner.getOwner().isEuropean()) {
	        if (combatSides.defenderTile.doesTileHaveNavyUnit()) {
	            if (loser.hasRepairLocation()) {
	                combatResultDetails.add(CombatResultDetails.DAMAGE_COLONY_SHIPS);
	            } else {
                    combatResultDetails.add(CombatResultDetails.SINK_COLONY_SHIPS);
	            }
	        }
	        if (loserMustDie) {
	            combatResultDetails.add(CombatResultDetails.SLAUGHTER_UNIT);
	        }
	        if (loser.getOwner().isEuropean()) {
	            combatResultDetails.add(CombatResultDetails.CAPTURE_COLONY);
	        }
	    } else {
	        Colony colony = combatSides.defenderTile.getSettlement().getColony();
	        if (!greatResult && canColonyBePillaged(colony)) {
	            combatResultDetails.add(CombatResultDetails.PILLAGE_COLONY);
	        } else {
	            if (colony.getColonyUnitsCount() > 1 || loser.getTileLocationOrNull() != null) {
	                // Treat as ordinary combat
	                loserMustDie = true;
	                combatResultDetails.add(CombatResultDetails.SLAUGHTER_UNIT);
	            } else {
	                combatResultDetails.add(CombatResultDetails.SLAUGHTER_UNIT);
	                combatResultDetails.add(CombatResultDetails.DESTROY_COLONY);
	            }
	        }
	    }
	}

	private boolean canColonyBePillaged(Colony colony) {
	     return 
             !colony.hasStockade() 
             && winner.hasAbility(Ability.PILLAGE_UNPROTECTED_COLONY)
             && (colony.hasBurnableBuildings() 
                     || colony.tile.doesTileHaveNavyUnit() 
                     || (colony.hasLootableGoods() && winner.canCarryGoods() && winner.hasSpaceForAdditionalCargo())
                     || colony.getOwner().hasGold()
                );
	}
	
    // Attacking and defeating the defender of a native
    // settlement with a mission may yield converts but
    // also may provoke the burning of all missions.
    // Native settlements fall when there are no units
    // present either in-settlement or on the settlement
    // tile.
	private void indianSettlementCombatResultDetails() {
		// TODO:
	}
}
