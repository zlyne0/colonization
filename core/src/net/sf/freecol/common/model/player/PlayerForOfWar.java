package net.sf.freecol.common.model.player;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.BooleanMap;
import promitech.colonization.SpiralIterator;

public class PlayerForOfWar {
    
    private BooleanMap fogOfWar;
    private SpiralIterator spiralIterator;

    public void initFromMap(Map map, Player player) {
        fogOfWar = new BooleanMap(map, true);
        spiralIterator = new SpiralIterator(map.width, map.height);
    }

    void removeFogOfWar(int x, int y) {
        fogOfWar.set(x, y, false);
    }

    void removeFogOfWar(Tile tile) {
    	fogOfWar.set(tile.x, tile.y, false);
    }
    
    public void removeFogOfWar() {
    	fogOfWar.reset(false);
    }

    public boolean hasFogOfWar(Tile tile) {
        return fogOfWar.isSet(tile.x, tile.y);
    }
    
    public boolean hasFogOfWar(int x, int y) {
        return fogOfWar.isSet(x, y);
    }
    
    public void resetFogOfWar(Player player) {
        fogOfWar.reset(true);
        
        fogOfWarForUnits(player);
        fogOfWarForSettlements(player);
    }

    private void fogOfWarForSettlements(Player player) {
        for (Settlement settlement : player.settlements.entities()) {
            int visibleRadius = settlement.settlementType.getVisibleRadius();
            initFogOfWarForNeighboursTiles(player, settlement.tile, visibleRadius);
        }
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
        while (spiralIterator.hasNext()) {
        	if (player.isTileExplored(spiralIterator.getX(), spiralIterator.getY())) {
        		removeFogOfWar(spiralIterator.getX(), spiralIterator.getY());
        	}
            spiralIterator.next();
        }
    }
}
