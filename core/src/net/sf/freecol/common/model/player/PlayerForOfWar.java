package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.SpiralIterator;
import promitech.map.Boolean2dArray;

public class PlayerForOfWar {
	private Boolean2dArray fogOfWar;
    
    private SpiralIterator spiralIterator;

    public void initFromMap(Map map, Player player) {
        fogOfWar = new Boolean2dArray(map.width, map.height, true);
        spiralIterator = new SpiralIterator(map.width, map.height);
    }

    void removeFogOfWar(int x, int y) {
    	fogOfWar.set(x, y, false);
    }

    boolean removeFogOfWar(Tile tile) {
    	return fogOfWar.setAndReturnDifference(tile.x, tile.y, false);
    }
    
    public boolean removeFogOfWar(int tileCoordsIndex) {
    	return fogOfWar.setAndReturnDifference(tileCoordsIndex, false);
    }
    
    public void removeFogOfWar() {
    	fogOfWar.set(false);
    }

    public boolean hasFogOfWar(Tile tile) {
    	return fogOfWar.get(tile.x, tile.y);
    }
    
    public boolean hasFogOfWar(int x, int y) {
    	return fogOfWar.get(x, y);
    }
    
    public void resetFogOfWar(Game game, Player player) {
        fogOfWar.set(true);
        
        fogOfWarForUnits(player);
        fogOfWarForSettlements(player);
        fogOfWarForMissionary(player, game);
    }
    
    private void fogOfWarForMissionary(Player player, Game game) {
    	if (player.isEuropean()) {
    		for (Player indianPlayer : game.players.entities()) {
    			if (indianPlayer.isIndian()) {
    				for (Settlement settlement : indianPlayer.settlements.entities()) {
    					IndianSettlement is = settlement.getIndianSettlement();
    					if (is.hasMissionary(player)) {
    						fogOfWarForMissionary(is, player);
    					}
    				}
    			}
    		}
    	}
    }
    
    public void fogOfWarForMissionary(IndianSettlement is, Player player) {
    	int radius = is.getMissionary().lineOfSight(is.settlementType);
    	initFogOfWarForNeighboursTiles(player, is.tile, radius);
    }

    private void fogOfWarForSettlements(Player player) {
        for (Settlement settlement : player.settlements.entities()) {
        	resetFogOfWarForSettlement(player, settlement);
        }
    }
    
    public void resetFogOfWarForSettlement(Player player, Settlement settlement) {
    	int visibleRadius = settlement.settlementType.getVisibleRadius();
    	initFogOfWarForNeighboursTiles(player, settlement.tile, visibleRadius);
    }

    private void fogOfWarForUnits(Player player) {
        for (Unit unit : player.units.entities()) {
        	Tile unitTile = unit.getTileLocationOrNull();
        	if (unitTile == null) {
        		continue;
        	}
            int radius = unit.lineOfSight();
            initFogOfWarForNeighboursTiles(player, unitTile, radius);
        }
    }
    
    private void initFogOfWarForNeighboursTiles(Player player, Tile tile, int radius) {
        removeFogOfWar(tile);
        spiralIterator.reset(tile.x, tile.y, true, radius);
        int coordsIndex;
        while (spiralIterator.hasNext()) {
        	coordsIndex = spiralIterator.getCoordsIndex();
        	if (player.isTileExplored(coordsIndex)) {
        		removeFogOfWar(coordsIndex);
        	}
            spiralIterator.next();
        }
    }
}
