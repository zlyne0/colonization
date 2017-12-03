package promitech.colonization.gamelogic.combat;

import static promitech.colonization.gamelogic.combat.CombatAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileAssert;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitAssert;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.FoundingFather;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.gamelogic.combat.Combat.CombatResult;
import promitech.colonization.gamelogic.combat.Combat.CombatResultDetails;
import promitech.colonization.savegame.SaveGameParser;

public class CombatTest {

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
	public void navyAttackerWinWithoutCargo() throws Exception {
		// given
    	Unit dutchPrivater = dutch.units.getById("unit:6900");
    	dutchPrivater.getGoodsContainer().decreaseAllToZero();
    	
    	Tile attackTile = game.map.getTile(12, 80);
    	spanishPrivateer.changeUnitLocation(attackTile);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(dutchPrivater, attackTile);
    	combat.generateGreatWin();
    	combat.processAttackResult();

		// then
    	assertThat(combat)
    		.hasPowers(12f, 8.0f, 0.6f)
    		.hasResult(CombatResult.WIN, true)
    		.hasDetails(CombatResultDetails.SINK_SHIP_ATTACK);
    	
    	UnitAssert.assertThat(spanishPrivateer)
    		.isDisposed()
    		.notExistsOnTile(attackTile);
	}

    @Test 
	public void navyAttackerWinAndCaptureCargo() throws Exception {
		// given
    	Unit dutchPrivater = dutch.units.getById("unit:6900");
    	dutchPrivater.getGoodsContainer().decreaseAllToZero();
    	
    	Tile attackTile = game.map.getTile(12, 80);
    	spanishPrivateer.changeUnitLocation(attackTile);
    	spanishPrivateer.getGoodsContainer().decreaseAllToZero();
    	spanishPrivateer.getGoodsContainer().increaseGoodsQuantity(GoodsType.MUSKETS, 100);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(dutchPrivater, attackTile);
    	combat.generateGreatWin();
    	combat.processAttackResult();

		// then
    	assertThat(combat)
    		.hasPowers(12f, 7.0f, 0.63f)
    		.hasResult(CombatResult.WIN, true)
    		.hasDetails(CombatResultDetails.SINK_SHIP_ATTACK, CombatResultDetails.LOOT_SHIP);
    	
    	UnitAssert.assertThat(spanishPrivateer)
    		.isDisposed()
    		.notExistsOnTile(attackTile);
    	UnitAssert.assertThat(dutchPrivater)
    		.hasGoods(GoodsType.MUSKETS, 100);
	}
    
    
    @Test 
	public void navyAttackerLoseWithoutCargo() throws Exception {
		// given
    	Unit dutchPrivater = dutch.units.getById("unit:6900");
    	dutchPrivater.getGoodsContainer().decreaseAllToZero();
    	Tile attackFromTile = dutchPrivater.getTile();
    	
    	Tile attackTile = game.map.getTile(12, 80);
    	spanishPrivateer.changeUnitLocation(attackTile);

		// when
    	Combat combat = new Combat();
    	combat.init(dutchPrivater, attackTile);
    	combat.generateGreatLoss();
    	combat.processAttackResult();

		// then
    	assertThat(combat)
			.hasPowers(12f, 8.0f, 0.6f)
			.hasResult(CombatResult.LOSE, true)
			.hasDetails(CombatResultDetails.SINK_SHIP_ATTACK);
	
		UnitAssert.assertThat(dutchPrivater)
			.isDisposed()
			.notExistsOnTile(attackFromTile);
	}
    
/*    
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

    private void navyDefenceColony(Colony colony) {
    	colony.addBuilding(Specification.instance.buildingTypes.getById("model.building.fort"));
		colony.updateColonyFeatures();
    	
		UnitType artilleryType = Specification.instance.unitTypes.getById(UnitType.ARTILLERY);
		UnitRole defaultUnitRole = Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID);
		Unit artillery = new Unit(Game.idGenerator.nextId(Unit.class), artilleryType, defaultUnitRole, dutch);
		Unit artillery2 = new Unit(Game.idGenerator.nextId(Unit.class), artilleryType, defaultUnitRole, dutch);
		
		artillery.changeUnitLocation(colony.tile);
		artillery2.changeUnitLocation(colony.tile);
    }
    
    @Test 
	public void colonyVsPirate() throws Exception {
		// given
    	Tile newAmsterdamTile = game.map.getSafeTile(24, 78);
    	Colony newAmsterdam = newAmsterdamTile.getSettlement().getColony();
    	
    	navyDefenceColony(newAmsterdam);
		
    	Tile seaTile = game.map.getSafeTile(24, 79);
    	spanishPrivateer.changeUnitLocation(seaTile);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(newAmsterdam, seaTile, spanishPrivateer);

		// then
    	assertThat(combat).hasPowers(14f, 8f, 0.63f);
	}
    
    @Test 
	public void colonyVsFregate() throws Exception {
		// given
    	Tile newAmsterdamTile = game.map.getSafeTile(24, 78);
    	Colony newAmsterdam = newAmsterdamTile.getSettlement().getColony();
    	
    	navyDefenceColony(newAmsterdam);
		
    	Tile seaTile = game.map.getSafeTile(24, 79);

    	Unit spanishFrigate = new Unit(
			Game.idGenerator.nextId(Unit.class), 
			Specification.instance.unitTypes.getById("model.unit.frigate"), 
			Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID), 
			spanish
		);
    	spanishFrigate.changeUnitLocation(newAmsterdamTile);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(newAmsterdam, seaTile, spanishFrigate);

		// then
    	assertThat(combat).hasPowers(14f, 16f, 0.46f);
	}
    
    @Test 
	public void colonyVsCaravel() throws Exception {
		// given
    	Tile newAmsterdamTile = game.map.getSafeTile(24, 78);
    	Colony newAmsterdam = newAmsterdamTile.getSettlement().getColony();
    	
    	navyDefenceColony(newAmsterdam);
		
    	Tile seaTile = game.map.getSafeTile(24, 79);

    	Unit spanishCaravel = new Unit(
			Game.idGenerator.nextId(Unit.class), 
			Specification.instance.unitTypes.getById("model.unit.caravel"), 
			Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID), 
			spanish
		);
    	spanishCaravel.changeUnitLocation(newAmsterdamTile);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(newAmsterdam, seaTile, spanishCaravel);

		// then
    	assertThat(combat).hasPowers(14f, 2f, 0.87f);
	}
*/	
}
