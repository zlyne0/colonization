package promitech.colonization.savegame;

import static org.junit.Assert.*;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.SettlementType;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.NationType;

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
        assertEquals(21, game.specification.goodsTypes.size());
        
        NationType arawakNationType = game.specification.nationTypes.getById("model.nationType.arawak");
        SettlementType settlementType = arawakNationType.settlementTypes.getById("model.settlement.village");
        assertNotNull(settlementType);
        
        assertEquals(13, game.players.size());
        
        Tile tile = game.map.getTile(31, 23);
        Unit tileUnit = tile.units.getById("unit:6449");
        assertNotNull(tileUnit);
        
        Unit unit = tileUnit.getUnitLocation().getUnits().first();
        assertNotNull(unit);
        assertEquals("unit:7049", unit.getId());
        
        Player player = game.players.getById("player:1");
        assertNotNull(player.getEurope());
        assertEquals("europe:2", player.getEurope().getId());
        
        
        verifySettlementsGoods(game);
    }

    private void verifySettlementsGoods(Game game) {
        Tile tile = game.map.getTile(24, 78);
        Colony colony = (Colony)tile.getSettlement();
        assertEquals(10, colony.getGoodsContainer().goods.size());
    }
}
