package promitech.colonization.savegame;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.TileType;
import net.sf.freecol.common.model.UnitType;

import org.junit.Assert;
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
        Assert.assertEquals(42, game.specification.unitTypes.size());
        Assert.assertEquals(23, game.specification.tileTypes.size());

        Assert.assertEquals(12, game.specification.resourceTypes.size());
        Assert.assertEquals(6, game.specification.tileImprovementTypes.size());
        Assert.assertEquals(11, game.specification.unitRoles.size());
        Assert.assertEquals(18, game.specification.nationTypes.size());
        Assert.assertEquals(25, game.specification.nations.size());
        
        Assert.assertEquals(13, game.players.size());
    }
}
