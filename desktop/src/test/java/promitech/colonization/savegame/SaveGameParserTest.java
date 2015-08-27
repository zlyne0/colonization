package promitech.colonization.savegame;

import static org.junit.Assert.*;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

public class SaveGameParserTest {

    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }
    
    @Test
    public void canLoadSaveGame() throws Exception {
        // given
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600.xml");
        
        // when
        Game game = saveGameParser.parse();

        // then
        assertEquals(42, game.specification.unitTypes.size());
        assertEquals(23, game.specification.tileTypes.size());

        assertEquals(12, game.specification.resourceTypes.size());
        assertEquals( 6, game.specification.tileImprovementTypes.size());
        assertEquals(11, game.specification.unitRoles.size());
        assertEquals(18, game.specification.nationTypes.size());
        assertEquals(25, game.specification.nations.size());
        
        assertEquals(13, game.players.size());
        
        Tile tile = game.map.getTile(31, 23);
        Unit tileUnit = tile.units.getById("unit:6449");
        assertNotNull(tileUnit);
        
        Unit unit = tileUnit.getUnitLocation().getUnits().first();
        assertNotNull(unit);
        assertEquals("unit:7049", unit.getId());
    }
}
