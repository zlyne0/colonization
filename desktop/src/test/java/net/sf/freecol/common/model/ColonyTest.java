package net.sf.freecol.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Colony.NoBuildReason;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import promitech.colonization.savegame.SaveGameParser;

public class ColonyTest {

	Game game;
	Player dutch;
	BuildingType stockadeType;
	
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }
	
    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	dutch = game.players.getById("player:1");
		stockadeType = Specification.instance.buildingTypes.getById("model.building.stockade");
    }
    
	@Test
	public void shouldDisallowBuildUnit() throws Exception {
		// given
        Colony colony = dutch.settlements.getById("colony:6528").getColony();
        
        UnitType freeColonist = Specification.instance.unitTypes.getById("model.unit.freeColonist");
        
		// when
        NoBuildReason noBuildReason = colony.getNoBuildReason(freeColonist);
		
		// then
        assertEquals(NoBuildReason.MISSING_BUILD_ABILITY, noBuildReason);
	}
	
}
