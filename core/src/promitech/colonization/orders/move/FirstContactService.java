package promitech.colonization.orders.move;

import java.util.HashMap;
import java.util.Map.Entry;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Stance;
import promitech.colonization.Direction;
import promitech.colonization.screen.map.hud.GUIGameModel;

class FirstContactService {

	private final MoveController moveController;
	private final GUIGameModel guiGameModel;
	
	public FirstContactService(MoveController moveController, GUIGameModel guiGameModel) {
		this.moveController = moveController;
		this.guiGameModel = guiGameModel;
	}
	
    void firstContact(Tile tile, Player player) {
    	// TODO: european first contact
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
                if (player.isAi() && neighbour.isAi()) {
                    player.changeStance(neighbour, Stance.PEACE);
                } else {
                    if (player.isAi()) {
                        moveController.showFirstContactDialog(neighbour, player);
                    } else {
                        moveController.showFirstContactDialog(player, neighbour);
                    }
                }
            }
        }
    }
	
	
}
