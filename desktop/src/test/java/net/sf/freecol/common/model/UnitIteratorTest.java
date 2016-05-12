package net.sf.freecol.common.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import net.sf.freecol.common.model.player.Player;

public class UnitIteratorTest {
    private Unit.UnitPredicate predicate = new Unit.UnitPredicate() {
        @Override
        public boolean obtains(Unit unit) {
            return unit.getMovesLeft() > 0;
        }
    };
    
    @Test
    public void canIterateThroughtUnits() throws Exception {
        // given
        Player player = new Player("player:1");
        player.units.add(new Unit("unit:1"));
        player.units.add(new Unit("unit:2"));
        
        for (Unit unit : player.units.entities()) {
            unit.reduceMovesLeft(-1);
            unit.setLocation(new Tile("tile:1", 1, 1, new TileType("tileType:1", false), 1));
        }
        
        UnitIterator unitIterator = new UnitIterator(player, predicate);
        
        // when
        List<Unit> units = new ArrayList<Unit>();
        while (unitIterator.hasNext()) {
            Unit unit = unitIterator.next();
            units.add(unit);
            unit.reduceMovesLeftToZero();
        }
        
        // then
        assertEquals(2, units.size());
    }

}
