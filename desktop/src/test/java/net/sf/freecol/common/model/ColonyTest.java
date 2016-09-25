package net.sf.freecol.common.model;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Colony.NoBuildReason;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.SaveGameParser;

public class ColonyTest {

    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }
	
	@Test
	public void shouldDisallowBuildUnit() throws Exception {
		// given
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
        Game game = saveGameParser.parse();
		
        Player player = game.players.getById("player:1");

        Colony colony = player.settlements.getById("colony:6528").getColony();
        
        UnitType freeColonist = Specification.instance.unitTypes.getById("model.unit.freeColonist");
        
		// when
        NoBuildReason noBuildReason = colony.getNoBuildReason(freeColonist);
		
		// then
        assertEquals(NoBuildReason.MISSING_BUILD_ABILITY, noBuildReason);
	}

}
