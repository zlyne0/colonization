package promitech.colonization.orders.combat;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.UnitTypeChange;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import promitech.colonization.Randomizer;
import promitech.colonization.orders.combat.Combat.CombatResultDetails;

class CombatResolver {

	protected List<CombatResultDetails> combatResultDetails = new ArrayList<Combat.CombatResultDetails>(5);
	private boolean greatResult;
	private boolean loserMustDie;
    protected boolean moveAfterAttack = false;
	
	public void init(boolean greatResult, CombatSides combatSides) {
		combatResultDetails.clear();
		this.moveAfterAttack = false;
		this.greatResult = greatResult;
		
		resolve(combatSides);
		
		this.moveAfterAttack = determineMoveAfterAttack();
	}

	public void initNoResult() {
	    moveAfterAttack = false;
		combatResultDetails.clear();
	}
	
	private boolean determineMoveAfterAttack() {
	    for (CombatResultDetails c : combatResultDetails) {
	        if (c == CombatResultDetails.CAPTURE_COLONY || c == CombatResultDetails.DESTROY_COLONY || c == CombatResultDetails.DESTROY_SETTLEMENT) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private void resolve(CombatSides combatSides) {
		loserMustDie = combatSides.loser.hasAbility(Ability.DISPOSE_ON_COMBAT_LOSS);
		
		if (combatSides.loser.isNaval()) {
			if (combatSides.winner.isNaval() 
					&& combatSides.winner.hasAbility(Ability.CAPTURE_GOODS) 
					&& combatSides.loser.hasGoodsCargo()) {
				combatResultDetails.add(CombatResultDetails.LOOT_SHIP);
			}
			if (greatResult || loserMustDie || !combatSides.loser.hasRepairLocation() || combatSides.loser.isBeached()) {
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
					indianSettlementCombatResultDetails(combatSides);
				}
			} else {
				landCombatResultDetails(combatSides);
			}
		}
		unitPromotion(greatResult, combatSides);
	}

	private void landCombatResultDetails(CombatSides combatSides) {
	    if (combatSides.isDefenderLoser() && combatSides.hasDefenderAutoArmRole()) {
	        
	        if (combatSides.winner.canCaptureEquipment(combatSides.getDefenderAutoArmRole())) {
	            combatResultDetails.add(CombatResultDetails.CAPTURE_AUTOEQUIP);
	            combatResultDetails.add(CombatResultDetails.LOSE_AUTOEQUIP);
	        } else {
	            combatResultDetails.add(CombatResultDetails.LOSE_AUTOEQUIP);
	        }
	        if (loserMustDie) {
	            combatResultDetails.add(CombatResultDetails.SLAUGHTER_UNIT);
	        } else if (combatSides.loser.hasAbility(Ability.DEMOTE_ON_ALL_EQUIPMENT_LOST)) {
	            combatResultDetails.add(CombatResultDetails.DEMOTE_UNIT);
	        }
	        
	    } else if (loserMustDie) {
			combatResultDetails.add(CombatResultDetails.SLAUGHTER_UNIT);
		} else if (combatSides.loser.unitRole.isOffensive()) {
			if (combatSides.winner.canCaptureEquipment(combatSides.loser)) {
				combatResultDetails.add(CombatResultDetails.CAPTURE_EQUIP);
			} else {
				combatResultDetails.add(CombatResultDetails.LOSE_EQUIP);
			}
			
			if (combatSides.loser.losingEquipmentKillsUnit()) {
				combatResultDetails.add(CombatResultDetails.SLAUGHTER_UNIT);
			} else if (combatSides.loser.losingEquipmentDemotesUnit()) {
				combatResultDetails.add(CombatResultDetails.DEMOTE_UNIT);
			}
			
		// But some can be captured.
		} else if (combatSides.loser.hasAbility(Ability.CAN_BE_CAPTURED) 
		        && combatSides.winner.hasAbility(Ability.CAPTURE_UNITS) 
		        && !combatSides.combatAmphibious) {
			combatResultDetails.add(CombatResultDetails.CAPTURE_UNIT);
			
		// Or losing just causes a demotion.
		} else if (combatSides.loser.canUpgradeByChangeType(ChangeType.DEMOTION)) {
				combatResultDetails.add(CombatResultDetails.DEMOTE_UNIT);
		} else {
			// default
			combatResultDetails.add(CombatResultDetails.SLAUGHTER_UNIT);
		}
	}

	private void unitPromotion(boolean greatResult, CombatSides combatSides) {
		UnitTypeChange promotion = combatSides.winner.unitType.getUnitTypeChange(
			ChangeType.PROMOTION, 
			combatSides.winner.getOwner()
		);
		if (promotion == null) {
			return;
		}
		if (combatSides.winner.hasAbility(Ability.AUTOMATIC_PROMOTION)) {
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
	    if (combatSides.loser.isDefensiveUnit() || combatSides.isDefenderLoser() && combatSides.hasDefenderAutoArmRole()) {
	        if (combatSides.isDefenderLoser() && combatSides.hasDefenderAutoArmRole()) {
	            combatResultDetails.add(CombatResultDetails.AUTOEQUIP_UNIT);
	        }
	        landCombatResultDetails(combatSides);
	        return;
	    }
	    
	    if (combatSides.winner.getOwner().isEuropean()) {
	        if (combatSides.defenderTile.doesTileHaveNavyUnit()) {
	            if (combatSides.loser.hasRepairLocation()) {
	                combatResultDetails.add(CombatResultDetails.DAMAGE_COLONY_SHIPS);
	            } else {
                    combatResultDetails.add(CombatResultDetails.SINK_COLONY_SHIPS);
	            }
	        }
	        if (loserMustDie) {
	            combatResultDetails.add(CombatResultDetails.SLAUGHTER_UNIT);
	        }
	        if (combatSides.loser.getOwner().isEuropean()) {
	            combatResultDetails.add(CombatResultDetails.CAPTURE_COLONY);
	        }
	    } else {
	        Colony colony = combatSides.defenderTile.getSettlement().getColony();
	        if (!greatResult && canColonyBePillaged(colony, combatSides)) {
	            combatResultDetails.add(CombatResultDetails.PILLAGE_COLONY);
	        } else {
	            if (colony.getColonyUnitsCount() > 1 || combatSides.loser.getTileLocationOrNull() != null) {
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

	private boolean canColonyBePillaged(Colony colony, CombatSides combatSides) {
	     return 
             !colony.hasStockade() 
             && combatSides.winner.hasAbility(Ability.PILLAGE_UNPROTECTED_COLONY)
             && (colony.hasBurnableBuildings() 
                     || colony.tile.doesTileHaveNavyUnit() 
                     || (colony.hasLootableGoods() && combatSides.winner.canCarryGoods() && combatSides.winner.hasSpaceForAdditionalCargo())
                     || colony.getOwner().hasGold()
                );
	}
	
    // Attacking and defeating the defender of a native
    // settlement with a mission may yield converts but
    // also may provoke the burning of all missions.
    // Native settlements fall when there are no units
    // present either in-settlement or on the settlement
    // tile.
	private void indianSettlementCombatResultDetails(CombatSides combatSides) {
	    IndianSettlement is = (IndianSettlement)combatSides.defenderTile.getSettlement();
	    int lose = 0;
	    
	    if (loserMustDie) {
	        combatResultDetails.add(CombatResultDetails.SLAUGHTER_UNIT);
	        lose++;
	    }
	    if (combatSides.isAttackerLose()) {
	        landCombatResultDetails(combatSides);
	    	return;
	    }
        if (Randomizer.instance().isHappen(getConvertProbability(combatSides.winner.getOwner()))) {
            if (!combatSides.combatAmphibious && is.hasMissionary(combatSides.winner.getOwner()) && isIndianSettlementHasMoreUnit(is, lose) ) {
                combatResultDetails.add(CombatResultDetails.CAPTURE_CONVERT);
                lose++;
            }
        } else {
            if (Randomizer.instance().isHappen(getBurnMissionaryPercentProbability())) {
                for (Settlement settlement : combatSides.loser.getOwner().settlements.entities()) {
                    if (((IndianSettlement)settlement).hasMissionary(combatSides.winner.getOwner())) {
                        combatResultDetails.add(CombatResultDetails.BURN_MISSIONS);
                    }
                }
            }
            if (!isIndianSettlementHasMoreUnit(is, lose)) {
                combatResultDetails.add(CombatResultDetails.DESTROY_SETTLEMENT);
            }
        }
	}
	
	private boolean isIndianSettlementHasMoreUnit(IndianSettlement is, int lose) {
	    return is.getUnits().size() + is.tile.getUnits().size() > lose;
	}
	
	private int getConvertProbability(Player player) {
	    int percentProb = Specification.options.getIntValue(GameOptions.NATIVE_CONVERT_PROBABILITY);
	    return (int)player.getFeatures().applyModifier(Modifier.NATIVE_CONVERT_BONUS, percentProb);
	}
	
	private int getBurnMissionaryPercentProbability() {
	    return Specification.options.getIntValue(GameOptions.BURN_PROBABILITY);
	}
}
