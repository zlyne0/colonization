package promitech.colonization.gamelogic.combat;

import static promitech.colonization.gamelogic.combat.CombatAssert.assertThat;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.FoundingFather;
import promitech.colonization.savegame.SaveGameParser;

public class NavyCombatTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	private Game game;
	private Player spanish;
	private Player dutch;
	
	private Unit spanishPrivateer;
	
    @BeforeClass
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
    }

    @Before
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	spanish = game.players.getById("player:133"); 
    	dutch = game.players.getById("player:1");
    	
    	spanishPrivateer = new Unit(
			Game.idGenerator.nextId(Unit.class), 
			Specification.instance.unitTypes.getById("model.unit.privateer"), 
			Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID), 
			spanish
		);
    }
	
    @Test 
	public void navyPrivateerWithoutCargoVsNavy() throws Exception {
		// given
    	Unit privateer = dutch.units.getById("unit:6900");
    	privateer.getGoodsContainer().decreaseAllToZero();
    	
    	Tile attackTile = game.map.getTile(12, 80);
    	spanishPrivateer.changeUnitLocation(attackTile);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(privateer, attackTile);

		// then
    	assertThat(combat).hasPowers(12f, 8.0f, 0.6f);
	}
    
    @Test 
	public void navyPrivateerWithCargoVsNavy() throws Exception {
		// given
    	Unit privateer = dutch.units.getById("unit:6900");
    	
    	Tile attackTile = game.map.getTile(12, 80);
    	spanishPrivateer.changeUnitLocation(attackTile);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(privateer, attackTile);

		// then
    	assertThat(combat).hasPowers(9f, 8f, 0.52f);
	}

    @Test 
	public void navyPrivateerWithDrakeWithoutCargoVsNavy() throws Exception {
		// given
    	FoundingFather francisDrake = Specification.instance.foundingFathers.getById("model.foundingFather.francisDrake");
    	dutch.addFoundingFathers(francisDrake);
    	
    	Unit privateer = dutch.units.getById("unit:6900");
    	privateer.getGoodsContainer().decreaseAllToZero();

    	spanish.addFoundingFathers(francisDrake);
    	Tile attackTile = game.map.getTile(12, 80);
    	spanishPrivateer.changeUnitLocation(attackTile);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(privateer, attackTile);

		// then
    	assertThat(combat).hasPowers(18f, 12.0f, 0.60f);
	}
    
	
}
