package net.sf.freecol.common.model.player;

import java.util.Collection;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.SpiralIterator;

public class PlayerForOfWar {
    
    private BooleanMap fogOfWar;
    private SpiralIterator spiralIterator;

    public void initFromMap(Map map, Player player) {
        fogOfWar = new BooleanMap(map, true);
        spiralIterator = new SpiralIterator(map.width, map.height);
        
        resetFogOfWar(player);
    }
    
    void removeFogOfWar(int x, int y) {
        fogOfWar.set(x, y, false);
    }
    
    public boolean hasFogOfWar(int x, int y) {
        return fogOfWar.isSet(x, y);
    }
    
    public void resetFogOfWar(Player player) {
        fogOfWar.reset(true);
        
        fogOfWarForUnits(player.units.entities());
        fogOfWarForSettlements(player.settlements.entities());
    }

    private void fogOfWarForSettlements(Collection<Settlement> settlements) {
        for (Settlement settlement : settlements) {
            int visibleRadius = settlement.settlementType.getVisibleRadius();
            initFogOfWarForNeighboursTiles(settlement.tile, visibleRadius);
        }
    }

    private void fogOfWarForUnits(Collection<Unit> units) {
        for (Unit unit : units) {
            int radius = unit.lineOfSight();
            initFogOfWarForNeighboursTiles(unit.getTile(), radius);
        }
    }
    
    private void initFogOfWarForNeighboursTiles(Tile tile, int radius) {
        removeFogOfWar(tile.x, tile.y);
        spiralIterator.reset(tile.x, tile.y, true, radius);
        while (spiralIterator.hasNext()) {
            removeFogOfWar(spiralIterator.getX(), spiralIterator.getY());
            spiralIterator.next();
        }
    }
}
