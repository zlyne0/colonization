package promitech.colonization.gamelogic.combat;

import static org.assertj.core.api.Assertions.assertThat;

import static promitech.colonization.gamelogic.combat.CombatAssert.assertThat;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import promitech.colonization.savegame.SaveGameParser;

public class LandCombatTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	private Game game;
	private Player spanish;
	
    @BeforeClass
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
    }

    @Before
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	spanish = game.players.getById("player:133"); 
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
    	combat.init(game, attacker, defenderUnit.getTile());

		// then
    	assertThat(combat).hasPowers(4.5f, 1.25f, 0.78f);
	}
    
    @Test
    public void artilleryInSettlementVsIndianUnit() throws Exception {
        // given
        Player defenderPlayer = game.players.getById("player:40");
        Unit defenderUnit = defenderPlayer.units.getById("unit:5967");

        Unit spanishAttacker = spanish.units.getById("unit:6659");
        spanishAttacker.setState(UnitState.ACTIVE);
        
        Combat combat = new Combat();
        // when
        combat.init(game, spanishAttacker, defenderUnit.getTile());
        
        // then
        assertThat(combat).hasPowers(15.75f, 1.25f, 0.92f);
    }
    
    @Test
    public void artilleryOutsideSettlementVsIndianUnit() throws Exception {
        // given
        Player defenderPlayer = game.players.getById("player:40");
        Unit defenderUnit = defenderPlayer.units.getById("unit:5967");

        Unit spanishAttacker = spanish.units.getById("unit:6659");
        spanishAttacker.setState(UnitState.ACTIVE);
        
        spanishAttacker.changeUnitLocation(game.map.getSafeTile(28, 57));
        
        Combat combat = new Combat();
        // when
        combat.init(game, spanishAttacker, defenderUnit.getTile());

        // then
        assertThat(combat).hasPowers(3.93f, 1.25f, 0.75f);
    }
    
    @Test 
	public void artilleryInsideSettlementVsIndianSettlement() throws Exception {
		// given
    	Unit spanishArtillery = spanish.units.getById("unit:6659");
    	
    	Tile indianSettlement = game.map.getTile(27,53);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(game, spanishArtillery, indianSettlement);

		// then
        assertThat(combat).hasPowers(15.75f, 3.0f, 0.84f);
	}

    @Test 
	public void artilleryOutsideSettlementVsIndianSettlement() throws Exception {
    	// given
    	Unit spanishArtillery = spanish.units.getById("unit:6659");
    	
    	// change unit location next to village
    	spanishArtillery.changeUnitLocation(game.map.getTile(28,52));
    	
		Tile indianSettlement = game.map.getTile(27,53);
    	
    	// when
    	Combat combat = new Combat();
    	combat.init(game, spanishArtillery, indianSettlement);
    	
    	// then
        assertThat(combat).hasPowers(15.75f, 3.0f, 0.84f);
    }
    
    @Test 
	public void dragonVsIndianSettlement() throws Exception {
		// given
    	Player attackerPlayer = game.players.getById("player:1");
    	Unit attacker = attackerPlayer.units.getById("unit:6764");
    	
    	Tile indianSettlement = game.map.getTile(27,53);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(game, attacker, indianSettlement);

		// then
        assertThat(combat).hasPowers(4.5f, 3.0f, 0.6f);
	}
    
    @Test 
	public void dragonVsColony() throws Exception {
		// given
    	Player attackerPlayer = game.players.getById("player:1");
    	Unit attacker = attackerPlayer.units.getById("unit:6764");

    	Tile colonyTile = game.map.getTile(29,58);

    	// when
    	Combat combat = new Combat();
    	combat.init(game, attacker, colonyTile);

		// then
        assertThat(combat).hasPowers(4.5f, 35.25f, 0.11f);
	}
    
    
    @Test 
	public void dragonVsEmptyColonyAndAutoArm() throws Exception {
		// given
    	Player attackerPlayer = game.players.getById("player:1");
    	Unit attacker = attackerPlayer.units.getById("unit:6764");

    	// remove artillery from colony
    	Unit spanishArtillery = spanish.units.getById("unit:6659");
    	spanishArtillery.changeUnitLocation(game.map.getTile(29,57));
    	
		Tile colonyTile = game.map.getTile(29, 58);

		// when
    	Combat combat = new Combat();
    	combat.init(game, attacker, colonyTile);

		// then
        assertThat(combat).hasPowers(4.5f, 5.5f, 0.45f);
	}

    @Test 
	public void dragonVsEmptyColonyWithoutAutoArm() throws Exception {
		// given
    	Player attackerPlayer = game.players.getById("player:1");
    	Unit attacker = attackerPlayer.units.getById("unit:6764");

    	// remove artillery from colony
    	Unit spanishArtillery = spanish.units.getById("unit:6659");
    	spanishArtillery.changeUnitLocation(game.map.getTile(29,57));
    	
		Tile colonyTile = game.map.getTile(29, 58);
		colonyTile.getSettlement().getGoodsContainer().decreaseAllToZero();

		// when
    	Combat combat = new Combat();
    	combat.init(game, attacker, colonyTile);

		// then
        assertThat(combat).hasPowers(4.5f, 4.5f, 0.5f);
	}
    
    
    @Test 
	public void indianVsColony() throws Exception {
		// given
    	Player indianPlayer = game.players.getById("player:40");
    	Unit indianUnit = indianPlayer.units.getById("unit:5967");
    	
    	Tile colonyTile = game.map.getTile(29, 58);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(game, indianUnit, colonyTile);

		// then
        assertThat(combat).hasPowers(1.5f, 70.5f, 0.03f);
	}

    @Test 
	public void indianVsColonyWithFort() throws Exception {
		// given
    	Player indianPlayer = game.players.getById("player:40");
    	Unit indianUnit = indianPlayer.units.getById("unit:5967");
    	
    	Tile colonyTile = game.map.getTile(29, 58);
    	
    	BuildingType fort = Specification.instance.buildingTypes.getById("model.building.fort");
		colonyTile.getSettlement().getColony().buildings.add(new Building(Game.idGenerator, fort));
		colonyTile.getSettlement().getColony().buildings.removeId("building:6914");
		colonyTile.getSettlement().getColony().updateColonyFeatures();
		
		// when
    	Combat combat = new Combat();
    	combat.init(game, indianUnit, colonyTile);

		// then
        assertThat(combat).hasPowers(1.5f, 58.25f, 0.02f);
	}
}
