package promitech.colonization.orders.combat;

import static promitech.colonization.orders.combat.CombatAssert.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitAssert;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.orders.combat.Combat;
import promitech.colonization.orders.combat.Combat.CombatResult;
import promitech.colonization.orders.combat.Combat.CombatResultDetails;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.resources.Messages;

public class NavyCombatTest {

	private Game game;
	private Player spanish;
	private Player dutch;
	
	private Unit spanishColonist;
    private Unit spanishColonist2;
	
    @BeforeAll
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
        Locale.setDefault(Locale.US);
        Messages.instance().load();
    }

    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	spanish = game.players.getById("player:133"); 
    	dutch = game.players.getById("player:1");
    	
    	spanishColonist = new Unit(
    	    Game.idGenerator.nextId(Unit.class), 
    	    Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST), 
    	    Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID), 
    	    spanish
	    );
        spanishColonist2 = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST), 
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
    	UnitFactory.create("model.unit.privateer", spanish, attackTile);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(game, privateer, attackTile);

		// then
    	assertThat(combat).hasPowers(12f, 8.0f, 0.6f);
	}
    
    @Test 
	public void navyPrivateerWithCargoVsNavy() throws Exception {
		// given
    	Unit privateer = dutch.units.getById("unit:6900");
    	
    	Tile attackTile = game.map.getTile(12, 80);
    	UnitFactory.create("model.unit.privateer", spanish, attackTile);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(game, privateer, attackTile);

		// then
    	assertThat(combat).hasPowers(9f, 8f, 0.52f);
	}

    @Test 
	public void navyPrivateerWithDrakeWithoutCargoVsNavy() throws Exception {
		// given
    	FoundingFather francisDrake = Specification.instance.foundingFathers.getById("model.foundingFather.francisDrake");
    	dutch.addFoundingFathers(game, francisDrake);
    	
    	Unit privateer = dutch.units.getById("unit:6900");
    	privateer.getGoodsContainer().decreaseAllToZero();

    	spanish.addFoundingFathers(game, francisDrake);
    	Tile attackTile = game.map.getTile(12, 80);
    	UnitFactory.create("model.unit.privateer", spanish, attackTile);    	
    	
		// when
    	Combat combat = new Combat();
    	combat.init(game, privateer, attackTile);

		// then
    	assertThat(combat).hasPowers(18f, 12.0f, 0.60f);
	}

    @Test 
	public void evadeBombardColonyVsPirate() throws Exception {
		// given
    	Tile newAmsterdamTile = game.map.getSafeTile(24, 78);
    	Colony newAmsterdam = newAmsterdamTile.getSettlement().asColony();
    	
    	createColonyNavyDefence(newAmsterdam);
		
    	Tile seaTile = game.map.getSafeTile(24, 79);
    	Unit spanishPrivateer = UnitFactory.create("model.unit.privateer", spanish, seaTile);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(game, newAmsterdam, seaTile, spanishPrivateer);
        combat.generateGreatLoss();
        combat.processAttackResult();

		// then
    	assertThat(combat).hasPowers(14f, 8f, 0.63f)
			.hasResult(CombatResult.EVADE_ATTACK, false)
			.hasDetails(CombatResultDetails.EVADE_BOMBARD);
        UnitAssert.assertThat(spanishPrivateer)
	        .isNotDisposed()
	        .isExistsOnTile(seaTile);
	}
    
    @Test 
	public void shouldMoveFregateToRepairLocationWhenColonyWinVsFregate() throws Exception {
		// given
    	Tile newAmsterdamTile = game.map.getSafeTile(24, 78);
    	Colony newAmsterdam = newAmsterdamTile.getSettlement().asColony();
    	
    	createColonyNavyDefence(newAmsterdam);
		
    	Tile seaTile = game.map.getSafeTile(24, 79);
    	Unit spanishFrigate = UnitFactory.create("model.unit.frigate", spanish, seaTile);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(game, newAmsterdam, seaTile, spanishFrigate);
        combat.generateOrdinaryWin();
        combat.processAttackResult();

		// then
    	assertThat(combat)
    		.hasPowers(14f, 16f, 0.46f)
			.hasResult(CombatResult.WIN, false)
			.hasDetails(CombatResultDetails.DAMAGE_SHIP_BOMBARD);
        UnitAssert.assertThat(spanishFrigate)
	        .isDamaged()
	        .isNotDisposed()
	        .isAtLocation(Europe.class)
	        .notExistsOnTile(seaTile);
	}
    
    @Test 
	public void colonyWinVsCaravel() throws Exception {
		// given
    	Tile newAmsterdamTile = game.map.getSafeTile(24, 78);
    	Colony newAmsterdam = newAmsterdamTile.getSettlement().asColony();
    	
    	createColonyNavyDefence(newAmsterdam);
		
    	Tile seaTile = game.map.getSafeTile(24, 79);
    	Unit spanishCaravel = UnitFactory.create("model.unit.caravel", spanish, seaTile);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(game, newAmsterdam, seaTile, spanishCaravel);
        combat.generateGreatWin();
        combat.processAttackResult();

		// then
    	assertThat(combat)
    		.hasPowers(14f, 2f, 0.87f)
    		.hasResult(CombatResult.WIN, true)
    		.hasDetails(CombatResultDetails.SINK_SHIP_BOMBARD);
    	UnitAssert.assertThat(spanishCaravel)
			.isDisposed()
			.notExistsOnTile(seaTile);
	}

    @Test 
	public void navyAttackerWinWithoutCargo() throws Exception {
		// given
    	Unit dutchPrivater = dutch.units.getById("unit:6900");
    	dutchPrivater.getGoodsContainer().decreaseAllToZero();
    	
    	Tile attackTile = game.map.getTile(12, 80);
    	Unit spanishPrivateer = UnitFactory.create("model.unit.privateer", spanish, attackTile);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(game, dutchPrivater, attackTile);
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
    	Unit spanishPrivateer = UnitFactory.create("model.unit.privateer", spanish, attackTile);
    	spanishPrivateer.getGoodsContainer().decreaseAllToZero();
    	spanishPrivateer.getGoodsContainer().increaseGoodsQuantity(GoodsType.MUSKETS, 100);
    	
		// when
    	Combat combat = new Combat();
    	combat.init(game, dutchPrivater, attackTile);
    	combat.generateGreatWin();
    	combat.processAttackResult();

		// then
    	assertThat(combat)
    		.hasPowers(12f, 7.0f, 0.63f)
    		.hasResult(CombatResult.WIN, true)
    		.hasDetails(CombatResultDetails.LOOT_SHIP, CombatResultDetails.SINK_SHIP_ATTACK);
    	
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
    	UnitFactory.create("model.unit.privateer", spanish, attackTile);

		// when
    	Combat combat = new Combat();
    	combat.init(game, dutchPrivater, attackTile);
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

    @Test 
    public void navyAttackerLoseAndBecomeDamage() throws Exception {
        // given
        Unit dutchPrivater = dutch.units.getById("unit:6900");
        dutchPrivater.getGoodsContainer().decreaseAllToZero();
        Tile attackFromTile = dutchPrivater.getTile();
        
        Tile attackTile = game.map.getTile(12, 80);
        Unit spanishPrivateer = UnitFactory.create("model.unit.privateer", spanish, attackTile);

        // when
        Combat combat = new Combat();
        combat.init(game, dutchPrivater, attackTile);
        combat.generateOrdinaryLoss();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(12f, 8.0f, 0.6f)
            .hasResult(CombatResult.LOSE, false)
            .hasDetails(CombatResultDetails.DAMAGE_SHIP_ATTACK);
    
        UnitAssert.assertThat(dutchPrivater)
            .isDamaged()
            .isNotDisposed()
            .isAtLocation(Europe.class)
            .notExistsOnTile(attackFromTile);
        UnitAssert.assertThat(spanishPrivateer)
            .isNotDisposed();
    }
    
    @Test
    public void navyAttackerDamageEnemyShip() throws Exception {
        // given
        Unit dutchPrivater = dutch.units.getById("unit:6900");
        dutchPrivater.getGoodsContainer().decreaseAllToZero();
        
        Tile attackTile = game.map.getTile(12, 80);
        Unit spanishPrivateer = UnitFactory.create("model.unit.privateer", spanish, attackTile);
        spanishPrivateer.getGoodsContainer().decreaseAllToZero();
        spanishPrivateer.getGoodsContainer().increaseGoodsQuantity(GoodsType.MUSKETS, 100);
        
        spanishColonist.embarkTo(spanishPrivateer);
        spanishColonist2.embarkTo(spanishPrivateer);
        
        // when
        Combat combat = new Combat();
        combat.init(game, dutchPrivater, attackTile);
        combat.generateOrdinaryWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(12f, 7.0f, 0.63f)
            .hasResult(CombatResult.WIN, false)
            .hasDetails(CombatResultDetails.LOOT_SHIP, CombatResultDetails.DAMAGE_SHIP_ATTACK);
        
        UnitAssert.assertThat(spanishPrivateer)
            .isDamaged()
            .hasNoGoods()
            .hasNoUnits()
            .hasNoMovesPoints()
            .isNotDisposed()
            .isAtLocation(Europe.class)
            .notExistsOnTile(attackTile);
        UnitAssert.assertThat(spanishColonist)
            .isDisposed();
        UnitAssert.assertThat(spanishColonist2)
            .isDisposed();        
        UnitAssert.assertThat(dutchPrivater)
            .isNotDisposed()
            .hasGoods(GoodsType.MUSKETS, 100);
    }
    
    private void createColonyNavyDefence(Colony colony) {
    	colony.addBuilding(Specification.instance.buildingTypes.getById("model.building.fort"));
		colony.updateColonyFeatures();
    	
		UnitFactory.create(UnitType.ARTILLERY, dutch, colony.tile);
		UnitFactory.create(UnitType.ARTILLERY, dutch, colony.tile);
    }
    
}
