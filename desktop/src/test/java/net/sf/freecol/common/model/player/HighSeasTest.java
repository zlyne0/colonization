package net.sf.freecol.common.model.player;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.savegame.SaveGameParser;

public class HighSeasTest {

    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new Lwjgl3Files();
    }
    
    @Test
    public void canCalculateSailTurnsWithoutFerdinandMagellan() throws Exception {
        // given
        Game game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
        
        Player player = game.players.getById("player:1");
        assertFalse(player.foundingFathers.containsId(FoundingFather.FERDINAND_MAGELLAN), "should not have father");
        
        Unit merchantman = player.units.getById("unit:6437");
        
        // when
        int sailTurns = merchantman.getSailTurns();

        // then
        assertEquals(3, sailTurns);
    }

    @Test
    public void canCalculateSailTurnsWithFerdinandMagellan() throws Exception {
        // given
        Game game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
        FoundingFather ferfinandMagellan = Specification.instance.foundingFathers.getById(FoundingFather.FERDINAND_MAGELLAN);

        Player player = game.players.getById("player:1");
        if (!player.foundingFathers.containsId(FoundingFather.FERDINAND_MAGELLAN)) {
            player.addFoundingFathers(game, ferfinandMagellan);
        }
        
        Unit merchantman = player.units.getById("unit:6437");
        
        // when
        int sailTurns = merchantman.getSailTurns();

        // then
        assertEquals(2, sailTurns);
    }
    
}
