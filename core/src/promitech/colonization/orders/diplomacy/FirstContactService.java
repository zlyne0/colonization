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
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.screen.map.hud.GUIGameModel;

public class FirstContactService {
	
	private final GUIGameModel guiGameModel;
	private final FirstContactController firstContactController;
	
	public FirstContactService(FirstContactController firstContactController, GUIGameModel guiGameModel) {
		this.firstContactController = firstContactController;
		this.guiGameModel = guiGameModel;
	}
	
    public void firstContact(Tile tile, Player player) {
        java.util.Map<String,Player> firstContactPlayers = null;
        if (tile.getType().isWater()) {
            return;
        }
        for (Direction direction : Direction.allDirections) {
            Tile neighbourTile = guiGameModel.game.map.getTile(tile, direction);
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
	
    SpeakToChiefResult scoutSpeakWithIndianSettlementChief(IndianSettlement is, Unit scout) {
		Tension tension = is.getTension(scout.getOwner());
		if (tension.getLevel() == Tension.Level.HATEFUL) {
        	scout.removeFromLocation();
        	scout.getOwner().removeUnit(scout);
			return SpeakToChiefResult.DIE;
		}
		
		SpeakToChiefResult result = null;
		if (is.hasAnyScouted()) {
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
				player.setTileAsExplored(coordsIndex);
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

	public LearnSkillResult learnSkill(IndianSettlement is, MoveContext moveContext) {
		Unit unit = moveContext.unit;
		
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
			moveContext.setUnitKilled();
			return LearnSkillResult.KILL;
		case ANGRY:
			return LearnSkillResult.NOTHING;
		default:
			unit.changeUnitType(is.getLearnableSkill());
			is.learnSkill(unit, Specification.options.getBoolean(GameOptions.ENHANCED_MISSIONARIES));
			return LearnSkillResult.LEARN;
		}
	}
}
