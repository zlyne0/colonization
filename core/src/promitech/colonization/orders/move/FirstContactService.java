package promitech.colonization.orders.move;

import java.util.HashMap;
import java.util.Map.Entry;

import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Stance;
import net.sf.freecol.common.model.player.Tension;
import net.sf.freecol.common.model.specification.Ability;
import promitech.colonization.Direction;
import promitech.colonization.Randomizer;
import promitech.colonization.SpiralIterator;
import promitech.colonization.screen.map.hud.GUIGameModel;

class FirstContactService {

	private final MoveController moveController;
	private final GUIGameModel guiGameModel;
	
	public FirstContactService(MoveController moveController, GUIGameModel guiGameModel) {
		this.moveController = moveController;
		this.guiGameModel = guiGameModel;
	}
	
    void firstContact(Tile tile, Player player) {
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
                        moveController.blockedShowFirstContactDialog(neighbour, player);
                    } else {
                        moveController.blockedShowFirstContactDialog(player, neighbour);
                    }
                }
            }
        }
    }
	
    enum SpeakToChiefResult {
    	DIE, NOTHING, EXPERT, TALES, BEADS
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
}
