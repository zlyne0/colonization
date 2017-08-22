package promitech.colonization.gamelogic.combat;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitTest;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.savegame.SaveGameParser;

public class CombatTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	
    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }
    
    @Test 
	public void testName() throws Exception {
		// given
    	Game game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");

    	Player attackerPlayer = game.players.getById("player:1");
    	Unit attacker = attackerPlayer.units.getById("unit:6764");
    	
    	Player defenderPlayer = game.players.getById("player:40");
    	Unit defenderUnit = defenderPlayer.units.getById("unit:5967");
    	
    	Player spanish = game.players.getById("player:133");
    	Unit spanishAttacker = spanish.units.getById("unit:6659");
    	
    	
    	Combat combat = new Combat();
    	combat.init(attacker, defenderUnit);
    	
    	System.out.println("dutch vs indian = " + combat.getOffencePower()
    			+ " vs " + combat.getDefencePower());
    	System.out.println("win probability " + combat.getWinPropability());

    	combat.init(spanishAttacker, defenderUnit);
    	System.out.println("spanish vs indian = " + combat.getOffencePower()
    			+ " vs " + combat.getDefencePower());
    	System.out.println("win probability " + combat.getWinPropability());
    	
		// when

		// then
	}
    
    // TODO: test case dragon vs indian settlement
    // TODO: test case dragon vs colony
    // TODO: test case colony vs pirate
    // TODO: test case colony vs fregate
    // TODO: test case colony vs caravel
    // TODO: test case altilery vs indian
    // TODO: test case altilery vs indian settlement
    // TODO: setlement Automatic defensive role (e.g. Revere)
}
