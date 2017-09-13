package promitech.colonization.gamelogic.combat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
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
	
	private Game game;
	
    @BeforeClass
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
    }

    @Before
    public void setup() throws Exception {
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
    	combat.init(attacker, defenderUnit.getTile());
    	
    	System.out.println("dutch vs indian = " + combat.getOffencePower()
    			+ " vs " + combat.getDefencePower());
    	System.out.println("win probability " + combat.getWinPropability());

    	combat.init(spanishAttacker, defenderUnit.getTile());
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
    	combat.init(attacker, defenderUnit.getTile());

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
        combat.init(spanishAttacker, defenderUnit.getTile());

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
        combat.init(spanishAttacker, defenderUnit.getTile());

        // then
        assertThat(combat.getOffencePower()).isEqualTo(2.62f, offset(0.01f));
        assertThat(combat.getDefencePower()).isEqualTo(1.25f, offset(0.01f));
        assertThat(combat.getWinPropability()).isEqualTo(0.67f, offset(0.01f));
    }
    
    @Test 
	public void artilleryInsideSettlementVsIndianSettlement() throws Exception {
		// given
    	Player spanish = game.players.getById("player:133");
    	Unit spanishArtillery = spanish.units.getById("unit:6659");
    	
    	Tile indianSettlement = game.map.getTile(27,53);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(spanishArtillery, indianSettlement);

		// then
        assertThat(combat.getOffencePower()).isEqualTo(10.5f, offset(0.01f));
        assertThat(combat.getDefencePower()).isEqualTo(2.0f, offset(0.01f));
        assertThat(combat.getWinPropability()).isEqualTo(0.84f, offset(0.01f));
	}

    @Test 
	public void artilleryOutsideSettlementVsIndianSettlement() throws Exception {
    	// given
    	Player spanish = game.players.getById("player:133");
    	Unit spanishArtillery = spanish.units.getById("unit:6659");
    	
    	// change unit location next to village
    	spanishArtillery.changeUnitLocation(game.map.getTile(28,52));
    	
		Tile indianSettlement = game.map.getTile(27,53);
    	
    	// when
    	Combat combat = new Combat();
    	combat.init(spanishArtillery, indianSettlement);
    	
    	// then
        assertThat(combat.getOffencePower()).isEqualTo(10.5f, offset(0.01f));
        assertThat(combat.getDefencePower()).isEqualTo(2.0f, offset(0.01f));
        assertThat(combat.getWinPropability()).isEqualTo(0.84f, offset(0.01f));
    }
    
    @Test 
	public void dragonVsIndianSettlement() throws Exception {
		// given
    	Player attackerPlayer = game.players.getById("player:1");
    	Unit attacker = attackerPlayer.units.getById("unit:6764");
    	
    	Tile indianSettlement = game.map.getTile(27,53);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(attacker, indianSettlement);

		// then
        assertThat(combat.getOffencePower()).isEqualTo(4.5f, offset(0.01f));
        assertThat(combat.getDefencePower()).isEqualTo(2.0f, offset(0.01f));
        assertThat(combat.getWinPropability()).isEqualTo(0.69f, offset(0.01f));
	}
    
    @Test 
	public void dragonVsColony() throws Exception {
		// given
    	Player attackerPlayer = game.players.getById("player:1");
    	Unit attacker = attackerPlayer.units.getById("unit:6764");

    	Tile colonyTile = game.map.getTile(29,58);

    	// when
    	Combat combat = new Combat();
    	combat.init(attacker, colonyTile);

		// then
        assertThat(combat.getOffencePower()).isEqualTo(4.5f, offset(0.01f));
        assertThat(combat.getDefencePower()).isEqualTo(22.5f, offset(0.01f));
        assertThat(combat.getWinPropability()).isEqualTo(0.16f, offset(0.01f));
	}
    
    
    @Test 
	public void dragonVsEmptyColony() throws Exception {
		// given
    	Player attackerPlayer = game.players.getById("player:1");
    	Unit attacker = attackerPlayer.units.getById("unit:6764");

    	// remove artillery from colony
    	Player spanish = game.players.getById("player:133");
    	Unit spanishArtillery = spanish.units.getById("unit:6659");
    	spanishArtillery.changeUnitLocation(game.map.getTile(29,57));
    	
		Tile colonyTile = game.map.getTile(29, 58);

		// when
    	Combat combat = new Combat();
    	combat.init(attacker, colonyTile);

		// then
        assertThat(combat.getOffencePower()).isEqualTo(4.5f, offset(0.01f));
        assertThat(combat.getDefencePower()).isEqualTo(4.5f, offset(0.01f));
        assertThat(combat.getWinPropability()).isEqualTo(0.5f, offset(0.01f));
	}
    
    @Test 
	public void indianVsColony() throws Exception {
		// given
    	Player indianPlayer = game.players.getById("player:40");
    	Unit indianUnit = indianPlayer.units.getById("unit:5967");
    	
    	Tile colonyTile = game.map.getTile(29, 58);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(indianUnit, colonyTile);

		// then
        assertThat(combat.getOffencePower()).isEqualTo(1.5f, offset(0.01f));
        assertThat(combat.getDefencePower()).isEqualTo(22.5f, offset(0.01f));
        assertThat(combat.getWinPropability()).isEqualTo(0.06f, offset(0.01f));
	}
    
/*    

    @Test 
	public void dragonVsEmptyColonyWithAutoArm() throws Exception {
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
*/	
}
