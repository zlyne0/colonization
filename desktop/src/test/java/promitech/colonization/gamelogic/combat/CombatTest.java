package promitech.colonization.gamelogic.combat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.assertj.core.data.Offset;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitTest;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.Modifier;
import promitech.colonization.savegame.SaveGameParser;

public class CombatTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	private static Game game;
	
    @BeforeClass
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
        game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    }
    
    @Test 
	public void prototype() throws Exception {
		// given

    	Player attackerPlayer = game.players.getById("player:1");
    	Unit attacker = attackerPlayer.units.getById("unit:6764");
    	
    	Player defenderPlayer = game.players.getById("player:40");
    	Unit defenderUnit = defenderPlayer.units.getById("unit:5967");
    	
    	Player spanish = game.players.getById("player:133");
    	Unit spanishAttacker = spanish.units.getById("unit:6659");
    	
    	
    	Combat combat = new Combat();
    	combat.init(attacker, defenderUnit);
    	combat.init(attacker, defenderUnit.getTile());
    	
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
    
    @Test 
	public void dragonUnitVsIndianUnit() throws Exception {
		// given
    	Player attackerPlayer = game.players.getById("player:1");
    	Unit attacker = attackerPlayer.units.getById("unit:6764");

    	Player defenderPlayer = game.players.getById("player:40");
    	Unit defenderUnit = defenderPlayer.units.getById("unit:5967");
    	
		// when
    	Combat combat = new Combat();
    	combat.init(attacker, defenderUnit);

		// then
        assertThat(combat.getOffencePower()).isEqualTo(4.5f, offset(0.01f));
        assertThat(combat.getDefencePower()).isEqualTo(1.25f, offset(0.01f));
        assertThat(combat.getWinPropability()).isEqualTo(0.78f, offset(0.01f));
	}
    
    @Test
    public void artilleryInSettlementVsIndianUnit() throws Exception {
        // given
        Player defenderPlayer = game.players.getById("player:40");
        Unit defenderUnit = defenderPlayer.units.getById("unit:5967");

        Player spanish = game.players.getById("player:133");
        Unit spanishAttacker = spanish.units.getById("unit:6659");
        spanishAttacker.setState(UnitState.ACTIVE);
        
        Combat combat = new Combat();
        // when
        combat.init(spanishAttacker, defenderUnit);

        // then
        assertThat(combat.getOffencePower()).isEqualTo(10.5f, offset(0.01f));
        assertThat(combat.getDefencePower()).isEqualTo(1.25f, offset(0.01f));
        assertThat(combat.getWinPropability()).isEqualTo(0.89f, offset(0.01f));
    }
    
    @Test
    public void artilleryOutsideSettlementVsIndianUnit() throws Exception {
        // given
        Player defenderPlayer = game.players.getById("player:40");
        Unit defenderUnit = defenderPlayer.units.getById("unit:5967");

        Player spanish = game.players.getById("player:133");
        Unit spanishAttacker = spanish.units.getById("unit:6659");
        spanishAttacker.setState(UnitState.ACTIVE);
        
        spanishAttacker.changeUnitLocation(game.map.getSafeTile(28, 57));
        
        Combat combat = new Combat();
        // when
        combat.init(spanishAttacker, defenderUnit);

        // then
        assertThat(combat.getOffencePower()).isEqualTo(2.62f, offset(0.01f));
        assertThat(combat.getDefencePower()).isEqualTo(1.25f, offset(0.01f));
        assertThat(combat.getWinPropability()).isEqualTo(0.67f, offset(0.01f));
    }
    
    @Test 
	public void artilleryOutsideSettlementVsIndianSettlement() throws Exception {
		// given

		// when

		// then
    	fail("not implements");
	}
    
    @Test 
	public void dragonVsIndainSettlement() throws Exception {
		// given

		// when

		// then
    	fail("not implements");
	}
    
    @Test 
	public void dragonVsColony() throws Exception {
		// given

		// when
		

		// then
    	fail("not implements");
	}
    
    @Test 
	public void dragonVsEmptyColony() throws Exception {
		// given

		// when
		

		// then
    	fail("not implements");
	}
    
    @Test 
	public void colonyVsPirate() throws Exception {
		// given

		// when
		

		// then
    	fail("not implements");
	}
    
    @Test 
	public void colonyVsFregate() throws Exception {
		// given

		// when
		

		// then
    	fail("not implements");
	}
    
    @Test 
	public void colonyVsCaravel() throws Exception {
		// given

		// when
		

		// then
    	fail("not implements");
	}
    
    @Test 
	public void indianVsColony() throws Exception {
		// given

		// when
		

		// then
    	fail("not implements");
	}
	
}
