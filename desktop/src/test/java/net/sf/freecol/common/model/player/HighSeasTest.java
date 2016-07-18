package net.sf.freecol.common.model.player;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.FoundingFather;
import promitech.colonization.savegame.SaveGameParser;

public class HighSeasTest {

    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }
    
    @Test
    public void canCalculateSailTurnsWithoutFerdinandMagellan() throws Exception {
        // given
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
        Game game = saveGameParser.parse();
        
        Player player = game.players.getById("player:1");
        assertNull("should not have father", player.foundingFathers.getByIdOrNull("model.foundingFather.ferdinandMagellan"));
        
        Unit merchantman = player.units.getById("unit:6437");
        
        
        HighSeas highSeas = player.getHighSeas();
        
        // when
        int sailTurns = highSeas.getSailTurns(merchantman);

        // then
        assertEquals(3, sailTurns);
    }

    @Test
    public void canCalculateSailTurnsWithFerdinandMagellan() throws Exception {
        // given
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
        Game game = saveGameParser.parse();

        Player player = game.players.getById("player:1");
        if (player.foundingFathers.getByIdOrNull("model.foundingFather.ferdinandMagellan") == null) {
            FoundingFather foundingFather = Specification.instance.foundingFathers.getById("model.foundingFather.ferdinandMagellan");
            player.addFoundingFathers(foundingFather);
            //player.foundingFathers.add(foundingFather);
        }
        
        Unit merchantman = player.units.getById("unit:6437");
        
        HighSeas highSeas = player.getHighSeas();
        
        // when
        int sailTurns = highSeas.getSailTurns(merchantman);

        // then
        assertEquals(2, sailTurns);
    }
    
}
