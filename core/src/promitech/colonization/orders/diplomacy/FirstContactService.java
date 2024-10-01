package promitech.colonization.orders.diplomacy;

import java.util.HashMap;
import java.util.Map.Entry;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Stance;
import net.sf.freecol.common.model.player.Tension;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import promitech.colonization.Direction;
import promitech.colonization.Randomizer;
import promitech.colonization.SpiralIterator;
import promitech.colonization.screen.map.hud.GUIGameModel;
import promitech.colonization.ui.resources.StringTemplate;

public class FirstContactService {
	
	private final GUIGameModel guiGameModel;
	private final FirstContactController firstContactController;
	
	public FirstContactService(FirstContactController firstContactController, GUIGameModel guiGameModel) {
		this.firstContactController = firstContactController;
		this.guiGameModel = guiGameModel;
	}
	
    public void firstContact(Tile moveDestTile, Player player) {
        java.util.Map<String,Player> firstContactPlayers = null;
        if (moveDestTile.getType().isWater()) {
            return;
        }
        for (Direction direction : Direction.allDirections) {
            Tile neighbourTile = guiGameModel.game.map.getTile(moveDestTile, direction);
            if (neighbourTile == null || neighbourTile.getType().isWater()) {
                continue;
            }
            Player neighbourTilePlayer = null;
            if (neighbourTile.hasSettlement()) {
                neighbourTilePlayer = neighbourTile.getSettlement().getOwner();
            } else {
                if (neighbourTile.getUnits().isNotEmpty()) {
                    neighbourTilePlayer = neighbourTile.getUnits().first().getOwner();
                }
            }
            if (neighbourTilePlayer == null || player.equalsId(neighbourTilePlayer) || player.hasContacted(neighbourTilePlayer)) {
                continue;
            }
            if (firstContactPlayers == null) {
                firstContactPlayers = new HashMap<String, Player>();
            }
            firstContactPlayers.put(neighbourTilePlayer.getId(), neighbourTilePlayer);
        }
        
        if (firstContactPlayers != null) {
            for (Entry<String, Player> neighbourPlayerEntry : firstContactPlayers.entrySet()) {
                Player neighbour = neighbourPlayerEntry.getValue();
                
                System.out.println("First contact. Player " + player.getId() + " met player = " + neighbour.getId());
                
                if (player.isAi() && neighbour.isAi()) {
                    player.changeStance(neighbour, Stance.PEACE);
                } else {
                    if (player.isAi()) {
                    	firstContactController.blockedShowFirstContactDialog(neighbour, player);
                    } else {
                    	firstContactController.blockedShowFirstContactDialog(player, neighbour);
                    }
                }
            }
        }
    }
	
    public SpeakToChiefResult scoutSpeakWithIndianSettlementChief(IndianSettlement is, Unit scout) {
		Tension tension = is.getTension(scout.getOwner());
		if (tension.getLevel() == Tension.Level.HATEFUL) {
        	scout.removeFromLocation();
        	scout.getOwner().removeUnit(scout);
			return SpeakToChiefResult.DIE;
		}
		
		SpeakToChiefResult result = null;
		if (is.isScouted()) {
			result = SpeakToChiefResult.NOTHING;
		} else if (is.getLearnableSkill() != null 
				&& !scout.hasAbility(Ability.EXPERT_SCOUT) 
				&& is.getLearnableSkill().hasAbility(Ability.EXPERT_SCOUT)) 
		{
			scout.changeUnitType(is.getLearnableSkill());
			result = SpeakToChiefResult.EXPERT;
		} else {
			// Choose tales 1/3 of the time, or if there are no beads.
			int gold = is.settlementType.getGift().randomValue();
			if (gold <= 0 || Randomizer.instance().isHappen(33)) {
				int radius = Math.max(scout.lineOfSight(), IndianSettlement.TALES_RADIUS); 
				result = SpeakToChiefResult.TALES;

				revealMap(guiGameModel.game.map, scout.getOwner(), is.tile, radius);
			} else {
				if (scout.hasAbility(Ability.EXPERT_SCOUT)) {
					gold = (gold * 11) / 10;
				}
				scout.getOwner().addGold(gold);
				is.getOwner().subtractGold(gold);
				result = SpeakToChiefResult.BEADS;
			}
		}
		is.scoutBy(scout.getOwner());
		scout.reduceMovesLeftToZero();
		return result;
	}
	
	private void revealMap(Map map, Player player, Tile source, int radius) {
		SpiralIterator spiralIterator = new SpiralIterator(map.width, map.height);
		spiralIterator.reset(source.x, source.y, true, radius);
		
		int coordsIndex = 0;
		while (spiralIterator.hasNext()) {
			coordsIndex = spiralIterator.getCoordsIndex();
			Tile tile = map.getSafeTile(spiralIterator.getX(), spiralIterator.getY());
			spiralIterator.next();
			
			if (tile.getType().isLand() || tile.isNextToLand() || tile.isOnSeaSide()) {
				player.getPlayerExploredTiles().setTileAsExplored(coordsIndex, guiGameModel.game.getTurn());
				player.fogOfWar.removeFogOfWar(coordsIndex);
			}
		}
	}
	
	int demandTributeFromIndian(Game game, IndianSettlement is, Unit unit) {
		int gold = is.demandTribute(game.getTurn(), unit.getOwner());
		if (gold > 0) {
			is.getOwner().subtractGold(gold);
			unit.getOwner().addGold(gold);
		}
		unit.reduceMovesLeftToZero();
		System.out.println("Player " + unit.getOwner().getId() + " demand " + gold + " gold from indian settlement " + is.getId());
		return gold;
	}

	public LearnSkillResult learnSkill(IndianSettlement is, Unit unit) {
		if (is.getLearnableSkill() == null) {
			throw new IllegalStateException("indian settlement " + is.getId() + " has no learnable skill");
		}
		if (!unit.unitType.canBeUpgraded(is.getLearnableSkill(), ChangeType.NATIVES)) {
			throw new IllegalStateException(
				"indian settlement " + is.getId() + 
				", unit " + unit.getId() + 
				" can not learn skill " + is.getLearnableSkill().getId()
			);
		}
		
		unit.reduceMovesLeftToZero();
		is.visitBy(unit.getOwner());
		
		switch (is.getTension(unit.getOwner()).getLevel()) {
		case HATEFUL:
			unit.getOwner().removeUnit(unit);
			return LearnSkillResult.KILL;
		case ANGRY:
			return LearnSkillResult.NOTHING;
		default:
			unit.changeUnitType(is.getLearnableSkill());
			is.learnSkill(unit, Specification.options.getBoolean(GameOptions.ENHANCED_MISSIONARIES));
			return LearnSkillResult.LEARN;
		}
	}

	public void establishMission(IndianSettlement is, Unit missionary) {
		is.visitBy(missionary.getOwner());
		missionary.reduceMovesLeftToZero();

		Tension tension = is.getTension(missionary.getOwner());
		switch (tension.getLevel()) {
		case HATEFUL:
		case ANGRY:
			missionary.getOwner().removeUnit(missionary);
			break;
		default:
			is.changeMissionary(missionary);
		}
	}
	
	public DenouceMissionResult denounceMission(IndianSettlement is, Unit newMissionary) {
		is.visitBy(newMissionary.getOwner());
		newMissionary.reduceMovesLeftToZero();
		
		float realProbability = Randomizer.instance().realProbability();
		float denounce = realProbability * is.missionaryOwner().getImmigration() / (newMissionary.getOwner().getImmigration() + 1);
		if (is.getMissionary().unit.hasAbility(Ability.EXPERT_MISSIONARY)) {
			denounce += 0.2;
		}
		if (newMissionary.hasAbility(Ability.EXPERT_MISSIONARY)) {
			denounce -= 0.2;
		}
		if (denounce < 0.5) { // Success, remove old mission and establish ours
			establishMission(is, newMissionary);
			return DenouceMissionResult.SUCCESS;
		}
		
		is.missionaryOwner().eventsNotifications.addMessageNotification(
			StringTemplate.template("indianSettlement.mission.enemyDenounce")
				.addStringTemplate("%enemy%", newMissionary.getOwner().getNationName())
				.add("%settlement%", is.getName())
				.addStringTemplate("%nation%", is.getOwner().getNationName())
		);
		
		newMissionary.getOwner().removeUnit(newMissionary);
		
		return DenouceMissionResult.FAILURE; 
	}
	
	public int inciteIndianPrice(Player nativePlayer, Player enemy, Unit missionary) {
        int payingValue = nativePlayer.getTension(missionary.getOwner()).getValue();
        int targetValue = nativePlayer.getTension(enemy).getValue();
        int goldToPay = (payingValue > targetValue) ? 10000 : 5000;
        goldToPay += 20 * (payingValue - targetValue);
        goldToPay = Math.max(goldToPay, 650);
        return goldToPay;
	}
	
	public void inciteIndian(Player nativePlayer, Player enemy, Unit missionary, int incitePrice) {
		nativePlayer.modifyTension(enemy, Tension.WAR_MODIFIER);
		enemy.modifyTension(missionary.getOwner(), Tension.TENSION_ADD_WAR_INCITER);
		nativePlayer.modifyTensionAndPropagateToAllSettlements(enemy, Tension.WAR_MODIFIER);
		missionary.getOwner().transferGoldToPlayer(incitePrice, nativePlayer);
	}
}
