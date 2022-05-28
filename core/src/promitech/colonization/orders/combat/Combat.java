package promitech.colonization.orders.combat;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.UnitLabel;
import net.sf.freecol.common.model.UnitLocation;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Stance;
import net.sf.freecol.common.model.player.Tension;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.RequiredGoods;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import promitech.colonization.Randomizer;
import promitech.colonization.orders.move.MoveContext;
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
	private Game game;
	private final List<StringTemplate> blockingCombatNotifications = new ArrayList<StringTemplate>();
	private MoveContext captureConvertMove;
	
	public void init(Game game, Colony colony, Tile defenderTile, Unit defender) {
	    blockingCombatNotifications.clear();
	    captureConvertMove = null;
		this.game = game;
		combatResult = null;
		greatResult = false;
		combatSides.init(colony, defenderTile, defender);
	}
	
	public void init(Game game, Unit attacker, Tile tile) {
	    blockingCombatNotifications.clear();
	    captureConvertMove = null;
		this.game = game;
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
			combatSides.winner = combatSides.attacker;
			combatSides.loser = combatSides.defender;
			combatResolver.init(greatResult, combatSides);
		} else {
			if (r < 0.8f * winVal + 0.2f && combatSides.canDefenderEvadeAttack()) {
				combatResult = CombatResult.EVADE_ATTACK;
				combatResolver.initNoResult();
			} else {
				combatResult = CombatResult.LOSE;
				greatResult = r >= 0.1f * winVal + 0.9f; // Great Loss
				combatSides.winner = combatSides.defender;
				combatSides.loser = combatSides.attacker;
				combatResolver.init(greatResult, combatSides);
			}
		}
	}

	private void bombardCombat(float r, float winVal) {
		combatResolver.combatResultDetails.clear();
		
		if (r <= winVal) {
			combatResult = CombatResult.WIN;
		    // Great wins occur at most in 1 in 3 of successful bombards,
		    // Good defences reduce this proportion.
			float diff = Math.max(3f, combatSides.getDefencePower() * 2f - combatSides.getOffencePower());
			greatResult = r < winVal / diff;
			
			combatSides.loser = combatSides.defender;
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
        modifyTension();
	    
	    System.out.println("combatResultDetails.size " + combatResolver.combatResultDetails.size());
		for (CombatResultDetails resultDetail : combatResolver.combatResultDetails) {
		    System.out.println(" - result " + resultDetail);
		    switch (resultDetail) {
		    case SINK_SHIP_ATTACK:
			case SINK_SHIP_BOMBARD: 
			    sinkShip(combatSides.loser);
			    break;
			case DAMAGE_SHIP_ATTACK:
				damageShip(combatSides.loser);
				break;
			case DAMAGE_SHIP_BOMBARD:
				damageShipInBombardment(combatSides.loser);
				break;
			case LOOT_SHIP:
				combatSides.loser.transferAllGoods(combatSides.winner);
				break;
			case SLAUGHTER_UNIT: {
                Player loserPlayer = combatSides.loser.getOwner();
                loserPlayer.removeUnit(combatSides.loser);
				if (combatSides.defenderTile.hasSettlement() && combatSides.defenderTile.getSettlement().isColony()) {
					combatSides.defenderTile.getSettlement().asColony().updateColonyPopulation();
					combatSides.defenderTile.getSettlement().asColony().updateModelOnWorkerAllocationOrGoodsTransfer();
				}
			} break;
			case PROMOTE_UNIT: combatSides.winner.changeUnitType(ChangeType.PROMOTION);
			break;
			case CAPTURE_EQUIP: captureEquipment();
			break;
			case DEMOTE_UNIT: combatSides.loser.changeUnitType(ChangeType.DEMOTION);
			break;
			case LOSE_EQUIP: combatSides.loser.downgradeRole(); 
		    break;
			case CAPTURE_UNIT: combatSides.winner.captureUnit(combatSides.loser); 
			break;
			
			case DAMAGE_COLONY_SHIPS: damageColonyShips();
			break;
			case PILLAGE_COLONY: { 
		        pillageColony();
			}
			break;
			
			case CAPTURE_COLONY: {
			    captureColony();
			} break;
			case SINK_COLONY_SHIPS: sinkColonyShips();
			break;
			case AUTOEQUIP_UNIT: // do nothing, in freecol its show message 
			case EVADE_ATTACK:   // do nothing, in freecol its show message
			break;
			case LOSE_AUTOEQUIP: loseAutoEquip();
			break;
			case CAPTURE_AUTOEQUIP: captureAutoEquip();
			break;
			
			case DESTROY_COLONY: {
			    destroyColony();
			} break;
			
			case BURN_MISSIONS: burnMissions();
			break;
			case CAPTURE_CONVERT: captureConvert();
			break;
			case DESTROY_SETTLEMENT: {
			    destroySettlement();
			}
			break;
			
			case EVADE_BOMBARD: // do nothing
			default:
				break;
			}
		}
	}

	private void modifyTension() {
        int attackerTension = 0;
        int defenderTension = 0;
        boolean burnedNativeCapital = false;

        if (combatSides.bombardmentColony != null) {
            return;
        }
        
        Player attackerPlayer = combatSides.attacker.getOwner();
        Player defenderPlayer = combatSides.defender.getOwner();
        
		for (CombatResultDetails resultDetail : combatResolver.combatResultDetails) {
			switch (resultDetail) {
			case SLAUGHTER_UNIT:
				if (combatSides.isAttackerWon()) {
					attackerTension -= Tension.TENSION_ADD_NORMAL;
					defenderTension += getSlaughterTension(combatSides.defender);
				} else {
					attackerTension += getSlaughterTension(combatSides.attacker);
					defenderTension -= Tension.TENSION_ADD_MAJOR;
				}
				break;

			case PILLAGE_COLONY:
				attackerTension -= Tension.TENSION_ADD_NORMAL;
				break;

			case CAPTURE_COLONY:
				defenderTension += Tension.TENSION_ADD_MAJOR;
				break;

			case DESTROY_COLONY:
				attackerTension -= Tension.TENSION_ADD_NORMAL;
				defenderTension += Tension.TENSION_ADD_MAJOR;
				break;

			case DESTROY_SETTLEMENT:
				IndianSettlement is = combatSides.defenderTile.getSettlement().asIndianSettlement();
				if (is.settlementType.isCapital()) {
					burnedNativeCapital = true;
				} else {
				    defenderTension += Tension.TENSION_ADD_MAJOR;
				}
				attackerTension -= Tension.TENSION_ADD_NORMAL;
				break;
			default:
				break;
			}
		}
		
		if (combatSides.attacker.hasAbility(Ability.PIRACY)) {
			defenderPlayer.setAttackedByPrivateers();
		} else if (combatSides.defender.hasAbility(Ability.PIRACY)) {
			// do nothing
		} else if (burnedNativeCapital) {
            defenderPlayer.getTension(attackerPlayer).surrende();
            attackerPlayer.changeStance(defenderPlayer, Stance.PEACE);
            
            for (Settlement settlement : defenderPlayer.settlements.entities()) {
                IndianSettlement is = settlement.asIndianSettlement();
                if (is.hasContact(attackerPlayer)) {
                    is.setTension(attackerPlayer, Tension.SURRENDERED);
                }
            }
		} else if (attackerPlayer.isEuropean() && defenderPlayer.isEuropean()) {
		    attackerPlayer.changeStance(defenderPlayer, Stance.WAR);
		} else {
		    if (attackerPlayer.isEuropean()) {
		        attackerPlayer.changeStance(defenderPlayer, Stance.WAR);
		    } else if (attackerPlayer.isIndian()) {
		        if (combatSides.isAttackerWon()) {
		            attackerTension -= Tension.TENSION_ADD_MINOR;
		        } else {
		            attackerTension += Tension.TENSION_ADD_MINOR;
		        }
		    }
		    
		    if (defenderPlayer.isEuropean()) {
		        defenderPlayer.changeStance(attackerPlayer, Stance.WAR);
		    } else if (defenderPlayer.isIndian()) {
		        if (combatSides.isAttackerWon()) {
		            defenderTension += Tension.TENSION_ADD_MINOR;
		        } else {
		            defenderTension -= Tension.TENSION_ADD_MINOR;
		        }
		    }
		    
		    defenderPlayer.modifyTension(attackerPlayer, defenderTension);
		    attackerPlayer.modifyTension(defenderPlayer, attackerTension);
		}
		
	}
	
	private void destroyColony() {
	    Colony colony = combatSides.defenderTile.getSettlement().asColony();
	    
	    int plunderGold = colonyPlunderGold(combatSides.winner.getOwner(), colony);
	    if (plunderGold > 0) {
	        colony.getOwner().subtractGold(plunderGold);
	        combatSides.winner.getOwner().addGold(plunderGold);
	    }
	    
        StringTemplate t = StringTemplate.template("model.unit.colonyBurning")
            .add("%colony%", colony.getName())
            .addStringTemplate("%nation%", combatSides.winner.getOwner().getNationName())
            .addStringTemplate("%unit%", UnitLabel.getPlainUnitLabel(combatSides.winner))
            .addAmount("%amount%", plunderGold);
        colony.getOwner().eventsNotifications.addMessageNotification(t);

        StringTemplate t2 = StringTemplate.template("model.unit.colonyBurning.other")
            .addStringTemplate("%nation%", colony.getOwner().getNationName())
            .add("%colony%", colony.getName())
            .addStringTemplate("%attackerNation%", combatSides.winner.getOwner().getNationName());
        sendNotificationToEuropeanPlayersExclude(t2, colony.getOwner());
        
	    
	    colony.removeFromMap(game);
	    colony.removeFromPlayer();
	}
	
	private String repairLocationLabel(UnitLocation unitLocation) {
	    if (unitLocation instanceof Settlement) {
	        return ((Settlement)unitLocation).getName();
	    }
	    if (unitLocation instanceof Europe) {
	        return "Europe";
	    }
	    throw new IllegalStateException("can not recognize unit location name for " + unitLocation);
	}
	
    private void pillageColony() {
        Colony colony = combatSides.defenderTile.getSettlement().asColony();
        List<Building> burnable = colony.createBurnableBuildingsList();
        List<Unit> navy = colony.tile.createNavyUnitsList();
        List<GoodsType> lootable = colony.createLootableGoodsList();
        
        int pillage = Randomizer.instance().randomInt(
            0, 
            burnable.size() + navy.size() + lootable.size() + (colony.getOwner().hasGold() ? 1 : 0)
        );
        
        if (pillage < burnable.size()) {
            Building building = burnable.get(pillage);
            indianPillageColonyBuilding(colony, building);
        } else if (pillage < burnable.size() + navy.size()) {
            Unit navyUnit = navy.get(pillage - burnable.size());
            
            if (navyUnit.hasRepairLocation()) {
                damageShip(navyUnit);
            } else {
                sinkShipWithNotification(navyUnit);
            }
            
        } else if (pillage < burnable.size() + navy.size() + lootable.size()) {
            GoodsType lootType = lootable.get(pillage - burnable.size() - navy.size());
            indianPillageColonyGoods(colony, lootType);
            
        } else {
            indianPillageColonyGold(colony);
        }
        
        StringTemplate t = StringTemplate.template("model.unit.indianRaid")
    		.addStringTemplate("%nation%", combatSides.winner.getOwner().getNationName())
    		.addStringTemplate("%colonyNation%", colony.getOwner().getNationName())
    		.add("%colony%", colony.getName());
        sendNotificationToEuropeanPlayersExclude(t, colony.getOwner());
    }

    private void sendNotificationToEuropeanPlayersExclude(StringTemplate str, Player exclude) {
        for (Player player : game.players.entities()) {
            if (!player.equals(exclude) && player.isLiveEuropeanPlayer()) {
                player.eventsNotifications.addMessageNotification(str);
            }
        }
    }
    
	private void indianPillageColonyBuilding(Colony colony, Building building) {
		colony.damageBuilding(building);
		
		StringTemplate t = StringTemplate.template("model.unit.buildingDamaged")
		    .addName("%building%", building.buildingType)
		    .add("%colony%", colony.getName())
		    .addStringTemplate("%enemyNation%", combatSides.winner.getOwner().getNationName())
		    .addStringTemplate("%enemyUnit%", UnitLabel.getPlainUnitLabel(combatSides.winner));
		colony.getOwner().eventsNotifications.addMessageNotification(t);
	}

    private void indianPillageColonyGold(Colony colony) {
        // plundered gold is already > 0
        int plunderGold = colonyPlunderGold(combatSides.winner.getOwner(), colony);
        combatSides.winner.getOwner().addGold(plunderGold);
        colony.getOwner().subtractGold(plunderGold);
        
        StringTemplate t = StringTemplate.template("model.unit.indianPlunder")
            .addStringTemplate("%enemyNation%", combatSides.winner.getOwner().getNationName())
            .addStringTemplate("%enemyUnit%", UnitLabel.getPlainUnitLabel(combatSides.winner))
            .addAmount("%amount%", plunderGold)
            .add("%colony%", colony.getName());
        colony.getOwner().eventsNotifications.addMessageNotification(t);
    }

    private void indianPillageColonyGoods(Colony colony, GoodsType lootType) {
        int goodsAmount = Math.min(colony.getGoodsContainer().goodsAmount(lootType) / 2, 50);
        AbstractGoods loot = new AbstractGoods(lootType.getId(), goodsAmount);
        
        colony.getGoodsContainer().decreaseGoodsQuantity(loot);
        if (combatSides.winner.hasSpaceForAdditionalCargo(loot)) {
            combatSides.winner.getGoodsContainer().increaseGoodsQuantity(loot);
        }
        
        StringTemplate t = StringTemplate.template("model.unit.goodsStolen")
            .addStringTemplate("%enemyNation%", combatSides.winner.getOwner().getNationName())
            .addStringTemplate("%enemyUnit%", UnitLabel.getPlainUnitLabel(combatSides.winner))
            .addAmount("%amount%", goodsAmount)
            .addName("%goods%", lootType)
            .add("%colony%", colony.getName());
        colony.getOwner().eventsNotifications.addMessageNotification(t);
    }

    private void loseAutoEquip() {
        UnitRole autoArmRole = combatSides.getDefenderAutoArmRole();
        Colony colony = combatSides.defenderTile.getSettlement().asColony();
        for (RequiredGoods requiredGoods : autoArmRole.requiredGoods.entities()) {
            colony.getGoodsContainer().decreaseGoodsQuantity(requiredGoods.goodsType.getId(), requiredGoods.amount);
        }
    }

	private void captureColony() {
		Colony colony = combatSides.defenderTile.getSettlement().asColony();
		Player winnerPlayer = combatSides.winner.getOwner();
		Player losserPlayer = colony.getOwner();
		
		int gold = 0;
		if (losserPlayer.hasGold()) {
		    gold = colonyPlunderGold(winnerPlayer, colony);
	        if (gold > 0) {
	            losserPlayer.subtractGold(gold);
	            winnerPlayer.addGold(gold);
	        }
		}
		colony.changeOwner(winnerPlayer);
		
		colony.updateColonyFeatures();
		colony.updateColonyPopulation();
		
		blockingCombatNotifications.add(
            StringTemplate.template("model.unit.colonyCaptured")
                .add("%colony%", colony.getName())
                .addAmount("%amount%", gold)
        );

        losserPlayer.eventsNotifications.addMessageNotification(
            StringTemplate.template("model.unit.colonyCapturedBy")
                .add("%colony%", colony.getName())
                .addAmount("%amount%", gold)
                .addStringTemplate("%player%", winnerPlayer.getNationName())
        );
	}
	
	private void sinkColonyShips() {
		for (Unit u : new ArrayList<Unit>(combatSides.defenderTile.getUnits().entities())) {
			if (u.isNaval()) {
				sinkShip(u);
			}
		}
	}
	
    private int colonyPlunderGold(Player attacker, Colony colony) {
        if (attacker.isIndian()) {
            int plunderGold = Math.max(1, colonyUpperRangePlunderGold(colony) / 5);
            return plunderGold;
        }
        int upper = colonyUpperRangePlunderGold(colony);
        if (upper <= 0) {
            return 0;
        }
        int gold = Randomizer.instance().randomInt(1, upper);
        return gold;
    }
    
	private int colonyUpperRangePlunderGold(Colony colony) {
	    return (colony.getOwner().getGold() * (colony.getUnits().size() + 1)) / (coloniesPopulation(colony.getOwner()) + 1);
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
		
		UnitLocation repairLocation = combatSides.loser.getRepairLocation();
		
		for (Unit unit : new ArrayList<Unit>(combatSides.defenderTile.getUnits().entities())) {
			if (unit.isNaval() && !(captureRepairing && unit.isDamaged())) {
				unit.makeUnitDamaged(repairLocation);
			}
		}
	}
	
	private void damageShipInBombardment(Unit ship) {
	    UnitLocation repairLocation = ship.getRepairLocation();
	    ship.makeUnitDamaged(repairLocation);
		
	    StringTemplate t = StringTemplate.template("model.unit.shipDamagedByBombardment")
    		.add("%colony%", combatSides.bombardmentColony.getName())
    		.addStringTemplate("%unit%", UnitLabel.getPlainUnitLabel(ship))
            .add("%repairLocation%", repairLocationLabel(repairLocation));
        ship.getOwner().eventsNotifications.addMessageNotification(t);
	}
	
	private void damageShip(Unit ship) {
	    UnitLocation repairLocation = ship.getRepairLocation();
	    ship.makeUnitDamaged(repairLocation);
	    
	    StringTemplate t = StringTemplate.template("model.unit.shipDamaged")
            .addStringTemplate("%unit%", UnitLabel.getPlainUnitLabel(ship))
            .addStringTemplate("%enemyNation%", combatSides.winner.getOwner().getNationName())
            .addStringTemplate("%enemyUnit%", UnitLabel.getPlainUnitLabel(combatSides.winner))
            .add("%repairLocation%", repairLocationLabel(repairLocation));
        ship.getOwner().eventsNotifications.addMessageNotification(t);
	}

    private void sinkShip(Unit ship) {
        Player loserPlayer = ship.getOwner();
        loserPlayer.removeUnit(ship);
    }
	
    private void sinkShipWithNotification(Unit ship) {
        StringTemplate t = StringTemplate.template("model.unit.shipSunk")
                .addStringTemplate("%unit%", UnitLabel.getPlainUnitLabel(ship))
                .addStringTemplate("%enemyNation%", combatSides.winner.getOwner().getNationName())
                .addStringTemplate("%enemyUnit%", UnitLabel.getPlainUnitLabel(combatSides.winner));
        ship.getOwner().eventsNotifications.addMessageNotification(t);
        
        sinkShip(ship);
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
        UnitRole lostRole = combatSides.loser.unitRole;
        combatSides.loser.downgradeRole();
        
        captureEquipmentFromUserRole(lostRole);
    }
    
    private void captureEquipmentFromUserRole(UnitRole loserRole) {
        UnitRole newRole = combatSides.winner.capturedEquipment(loserRole);
        if (newRole == null) {
            return;
        }
        if (combatSides.winner.getOwner().isIndian()) {
            indianTransferCapturedGoodToHomeSettlement(newRole);
        }
        combatSides.winner.changeRole(newRole);
    }

    private void indianTransferCapturedGoodToHomeSettlement(UnitRole roleEquipment) {
        // CHEAT: Immediately transferring the captured goods back to a potentially remote settlement
        // Apparently Col1 did it
        ProductionSummary equipment = UnitRole.requiredGoodsToChangeRole(combatSides.winner, roleEquipment);
        if (combatSides.winner.getIndianSettlementId() != null) {
            Settlement settlement = combatSides.winner.getOwner().settlements.getByIdOrNull(combatSides.winner.getIndianSettlementId());
            if (settlement != null) {
                for (Entry<String> goods : equipment.entries()) {
                    settlement.addGoods(goods.key, goods.value);
                }
            }
        }
    }

	private void burnMissions() {
        StringTemplate t = StringTemplate.template("model.unit.burnMissions")
            .addStringTemplate("%nation%", combatSides.winner.getOwner().getNationName())
    		.addStringTemplate("%enemyNation%", combatSides.loser.getOwner().getNationName());
        blockingCombatNotifications.add(t);
		
		for (Settlement settlement : combatSides.loser.getOwner().settlements.entities()) {
			IndianSettlement indianSettlement = settlement.asIndianSettlement();
			if (indianSettlement.hasMissionary(combatSides.winner.getOwner())) {
				indianSettlement.removeMissionary();
			}
		}
	}
	
	private void captureConvert() {
		IndianSettlement indianSettlement = combatSides.defenderTile.getSettlement().asIndianSettlement();
		Unit convert = indianSettlement.convertToDest(
			combatSides.winner.getTile(), 
			combatSides.winner.getOwner()
		);
		
        captureConvertMove = new MoveContext(
    		combatSides.defenderTile, 
    		combatSides.winner.getTile(), 
    		convert
		);
	}

	private void destroySettlement() {
		IndianSettlement is = combatSides.defenderTile.getSettlement().asIndianSettlement();
		
		int plunderGold = is.plunderGold(combatSides.winner);
		if (plunderGold > 0) {
			UnitFactory.createTreasureTrain(
				combatSides.winner.getOwner(),
				is.tile,
				plunderGold
			);
			
			StringTemplate t = StringTemplate.template("model.unit.indianTreasure")
				.add("%settlement%", is.getName())
				.addAmount("%amount%", plunderGold);
			blockingCombatNotifications.add(t);
		}
		
		for (Unit settlementPlayerUnit : is.getOwner().units.entities()) {
			if (settlementPlayerUnit.isBelongToIndianSettlement(is)) {
				settlementPlayerUnit.removeFromIndianSettlement();
			}
		}

		if (is.hasMissionaryNotPlayer(combatSides.winner.getOwner())) {
	        StringTemplate t = StringTemplate.template("indianSettlement.mission.destroyed")
                .add("%settlement%", is.getName());
	        is.missionaryOwner().eventsNotifications.addMessageNotification(t);
	        is.removeMissionary();
		}
		
		is.removeFromMap(game);
		is.removeFromPlayer();
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

    public List<StringTemplate> getBlockingCombatNotifications() {
        return blockingCombatNotifications;
    }

    public boolean isMoveAfterAttack() {
        return combatResolver.moveAfterAttack;
    }

	public MoveContext captureConvertMove() {
		return captureConvertMove;
	}
}
