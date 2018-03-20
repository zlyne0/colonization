package promitech.colonization.orders.combat;

import static net.sf.freecol.common.model.UnitAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static promitech.colonization.orders.combat.CombatAssert.assertThat;

import java.util.Locale;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyAssert;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.MapIdEntitiesAssert;
import net.sf.freecol.common.model.PlayerAssert;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileAssert;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitAssert;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.orders.combat.Combat.CombatResult;
import promitech.colonization.orders.combat.Combat.CombatResultDetails;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.resources.Messages;

public class ColonyCombatTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	private Game game;
    private Player spanish;
    private Player dutch;
    private Player aztec;
    private Combat combat = new Combat();
	
    @BeforeClass
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
        Locale.setDefault(Locale.US);
        Messages.instance().load();
    }

    @Before
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	spanish = game.players.getById("player:133"); 
    	dutch = game.players.getById("player:1");
    	aztec = game.players.getById("player:40");
    }
    
    @Test
    public void dragoonVsArmedColony() throws Exception {
        // given
        Unit dragoon = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
            Specification.instance.unitRoles.getById(UnitRole.DRAGOON),
            spanish
        );
        Tile freeTileNextToColony = game.map.getSafeTile(23, 79);
        dragoon.changeUnitLocation(freeTileNextToColony);
        
        Tile colonyTile = game.map.getSafeTile(24, 78);
        
        // when
        combat.init(game, dragoon, colonyTile);
        combat.generateGreatWin();
        combat.processAttackResult();
    
        // then
        assertThat(combat)
            .hasPowers(4.5f, 6.75f, 0.4f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.LOSE_EQUIP, CombatResultDetails.PROMOTE_UNIT);
    }
    
    @Test
    public void spanishVsEmptyColony() throws Exception {
        // given
        Unit dragoon = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
            Specification.instance.unitRoles.getById(UnitRole.DRAGOON),
            spanish
        );
        Tile freeTileNextToColony = game.map.getSafeTile(21, 80);
        dragoon.changeUnitLocation(freeTileNextToColony);
        
        Tile emptyColonyTile = game.map.getSafeTile(20, 79);
        Colony colony = emptyColonyTile.getSettlement().getColony();
        Unit frigate = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById("model.unit.frigate"), 
            Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID), 
            colony.getOwner()
        );
        frigate.changeUnitLocation(emptyColonyTile);
        
        MapIdEntities<Unit> colonyUnitsBeforeCombat = new MapIdEntities<>();
        colonyUnitsBeforeCombat.addAll(colony.getUnits());
        int spanishBeforeCombatGold = spanish.getGold();
        
        // when
        combat.init(game, dragoon, emptyColonyTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(4.5f, 2.25f, 0.66f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.DAMAGE_COLONY_SHIPS, CombatResultDetails.CAPTURE_COLONY, CombatResultDetails.PROMOTE_UNIT);
        UnitAssert.assertThat(frigate)
	        .isDamaged()
	        .isNotDisposed()
	        .isAtLocation(Europe.class)
	        .isOwnedBy(dutch)
        	.notExistsOnTile(emptyColonyTile);
        
        assertThat(spanish.getGold() > spanishBeforeCombatGold).isTrue();
        assertThat(spanish.settlements.containsId(colony)).isTrue();
        assertThat(dutch.settlements.containsId(colony)).isFalse();
        
        PlayerAssert.assertThat(spanish)
        	.containsUnits(colonyUnitsBeforeCombat);
        PlayerAssert.assertThat(dutch)
        	.notContainsUnits(colonyUnitsBeforeCombat);
    }

    @Test
    public void spanishSinkShipInColony() throws Exception {
        // given
        Unit dragoon = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
            Specification.instance.unitRoles.getById(UnitRole.DRAGOON),
            spanish
        );
        Tile freeTileNextToColony = game.map.getSafeTile(21, 80);
        dragoon.changeUnitLocation(freeTileNextToColony);
        
        Tile emptyColonyTile = game.map.getSafeTile(20, 79);
        Colony colony = emptyColonyTile.getSettlement().getColony();
        Unit frigate = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById("model.unit.frigate"), 
            Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID), 
            colony.getOwner()
        );
        frigate.changeUnitLocation(emptyColonyTile);
        removeUnitsButBesides(colony.tile, frigate);
        Unit defender = new Unit(
    		Game.idGenerator.nextId(Unit.class),
    		Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
    		Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
    		colony.getOwner()
		) {
        	@Override
        	public boolean hasRepairLocation() {
        		return false;
        	}
        };
        defender.changeUnitLocation(colony.tile);
        
        MapIdEntities<Unit> colonyUnitsBeforeCombat = new MapIdEntities<>();
        colonyUnitsBeforeCombat.addAll(colony.getUnits());
        int spanishBeforeCombatGold = spanish.getGold();
        
        // when
        combat.init(game, dragoon, emptyColonyTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(4.5f, 2.25f, 0.66f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.SINK_COLONY_SHIPS, CombatResultDetails.CAPTURE_COLONY, CombatResultDetails.PROMOTE_UNIT);
        UnitAssert.assertThat(frigate)
	        .isDisposed()
        	.notExistsOnTile(emptyColonyTile);
        
        assertThat(spanish.getGold() > spanishBeforeCombatGold).isTrue();
        assertThat(spanish.settlements.containsId(colony)).isTrue();
        assertThat(dutch.settlements.containsId(colony)).isFalse();
        
        PlayerAssert.assertThat(spanish)
        	.containsUnits(colonyUnitsBeforeCombat);
        PlayerAssert.assertThat(dutch)
        	.notContainsUnit(frigate)
        	.notContainsUnits(colonyUnitsBeforeCombat);
    }
    
    private void removeUnitsButBesides(Tile tile, Unit exclude) {
    	MapIdEntities<Unit> units = new MapIdEntities<>(tile.getUnits());
    	for (Unit u : units.entities()) {
    		if (u.notEqualsId(exclude)) {
    			tile.removeUnit(u);
    		}
    	}
    }
    
    @Test
    public void emptyColonyWithAutoRoleLoseVsDragoon() throws Exception {
        // given
        Unit dragoon = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
            Specification.instance.unitRoles.getById(UnitRole.DRAGOON),
            spanish
        );
        Tile freeTileNextToColony = game.map.getSafeTile(21, 80);
        dragoon.changeUnitLocation(freeTileNextToColony);
        
        Tile emptyColonyTile = game.map.getSafeTile(20, 79);
        Colony colony = emptyColonyTile.getSettlement().getColony();
        colony.addGoods(GoodsType.MUSKETS, 50);
        colony.getOwner().addFoundingFathers(Specification.instance.foundingFathers.getById("model.foundingFather.paulRevere"));
        colony.updateColonyFeatures();
        ProductionSummary goodsBefore = colony.getGoodsContainer().cloneGoods();

        // when
        combat.init(game, dragoon, emptyColonyTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(4.5f, 3.25f, 0.58f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.AUTOEQUIP_UNIT, CombatResultDetails.LOSE_AUTOEQUIP, CombatResultDetails.PROMOTE_UNIT);
        
        assertThat(colony.getGoodsContainer().goodsAmount(GoodsType.MUSKETS))
            .isEqualTo(goodsBefore.getQuantity(GoodsType.MUSKETS) - 50);
    }
    
    @Test
    public void emptyColonyWithAutoRoleWinVsDragoon() throws Exception {
        // given
        Unit dragoon = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
            Specification.instance.unitRoles.getById(UnitRole.DRAGOON),
            spanish
        );
        Tile freeTileNextToColony = game.map.getSafeTile(21, 80);
        dragoon.changeUnitLocation(freeTileNextToColony);
        
        Tile emptyColonyTile = game.map.getSafeTile(20, 79);
        Colony colony = emptyColonyTile.getSettlement().getColony();
        colony.addGoods(GoodsType.MUSKETS, 50);
        colony.getOwner().addFoundingFathers(Specification.instance.foundingFathers.getById("model.foundingFather.paulRevere"));
        colony.updateColonyFeatures();
        ProductionSummary goodsBefore = colony.getGoodsContainer().cloneGoods();

        // when
        combat.init(game, dragoon, emptyColonyTile);
        combat.generateGreatLoss();
        combat.processAttackResult();
        
        // then
        assertThat(combat)
            .hasPowers(4.5f, 3.25f, 0.58f)
            .hasResult(CombatResult.LOSE, true)
            .hasDetails(CombatResultDetails.LOSE_EQUIP);

        assertThat(colony.getGoodsContainer().goodsAmount(GoodsType.MUSKETS))
            .isEqualTo(goodsBefore.getQuantity(GoodsType.MUSKETS));
        
        assertThat(dragoon).isUnitRole(UnitRole.SOLDIER);
    }
    

    @Test
    public void emptyColonyWithAutoRoleLoseVsIndian() throws Exception {
        // given
        Unit brave = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById("model.unit.brave"),
            Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
            aztec
        );
        Tile freeTileNextToColony = game.map.getSafeTile(21, 80);
        brave.changeUnitLocation(freeTileNextToColony);
        
        Tile emptyColonyTile = game.map.getSafeTile(20, 79);
        Colony colony = emptyColonyTile.getSettlement().getColony();
        colony.addGoods(GoodsType.MUSKETS, 50);
        colony.getOwner().addFoundingFathers(Specification.instance.foundingFathers.getById("model.foundingFather.paulRevere"));
        colony.updateColonyFeatures();
        ProductionSummary goodsBefore = colony.getGoodsContainer().cloneGoods();

        // when
        combat.init(game, brave, emptyColonyTile);
        combat.generateGreatWin();
        combat.processAttackResult();
        
        // then
        assertThat(combat)
            .hasPowers(1.5f, 3.25f, 0.31f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.AUTOEQUIP_UNIT, CombatResultDetails.CAPTURE_AUTOEQUIP, CombatResultDetails.LOSE_AUTOEQUIP);

        assertThat(colony.getGoodsContainer().goodsAmount(GoodsType.MUSKETS))
            .isEqualTo(goodsBefore.getQuantity(GoodsType.MUSKETS) - 50);
        
        assertThat(brave).isUnitRole("model.role.armedBrave");
    }

    @Test
    public void emptyColonyWithAutoRoleWinVsIndian() throws Exception {
        // given
        Unit brave = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById("model.unit.brave"),
            Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
            aztec
        );
        Tile freeTileNextToColony = game.map.getSafeTile(21, 80);
        brave.changeUnitLocation(freeTileNextToColony);
        
        Tile emptyColonyTile = game.map.getSafeTile(20, 79);
        Colony colony = emptyColonyTile.getSettlement().getColony();
        colony.addGoods(GoodsType.MUSKETS, 50);
        colony.getOwner().addFoundingFathers(Specification.instance.foundingFathers.getById("model.foundingFather.paulRevere"));
        colony.updateColonyFeatures();
        ProductionSummary goodsBefore = colony.getGoodsContainer().cloneGoods();

        // when
        combat.init(game, brave, emptyColonyTile);
        combat.generateGreatLoss();
        combat.processAttackResult();
        
        // then
        assertThat(combat)
            .hasPowers(1.5f, 3.25f, 0.31f)
            .hasResult(CombatResult.LOSE, true)
            .hasDetails(CombatResultDetails.SLAUGHTER_UNIT);

        UnitAssert.assertThat(brave)
            .isDisposed();
        TileAssert.assertThat(freeTileNextToColony)
            .hasNotUnit(brave);
        assertThat(colony.getGoodsContainer().goodsAmount(GoodsType.MUSKETS))
            .isEqualTo(goodsBefore.getQuantity(GoodsType.MUSKETS));
    }
    
    
    @Test
    public void emptyColonyWithAutoRoleLoseVsIndian2() throws Exception {
        // given
        Unit brave = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById("model.unit.brave"),
            Specification.instance.unitRoles.getById("model.role.armedBrave"),
            aztec
        );
        Tile freeTileNextToColony = game.map.getSafeTile(21, 80);
        brave.changeUnitLocation(freeTileNextToColony);
        
        Tile emptyColonyTile = game.map.getSafeTile(20, 79);
        Colony colony = emptyColonyTile.getSettlement().getColony();
        colony.addGoods(GoodsType.MUSKETS, 50);
        colony.getOwner().addFoundingFathers(Specification.instance.foundingFathers.getById("model.foundingFather.paulRevere"));
        colony.updateColonyFeatures();
        ProductionSummary goodsBefore = colony.getGoodsContainer().cloneGoods();

        // when
        combat.init(game, brave, emptyColonyTile);
        combat.generateGreatWin();
        combat.processAttackResult();
        
        // then
        assertThat(combat)
            .hasPowers(4.5f, 3.25f, 0.58f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.AUTOEQUIP_UNIT, CombatResultDetails.LOSE_AUTOEQUIP);

        assertThat(colony.getGoodsContainer().goodsAmount(GoodsType.MUSKETS))
            .isEqualTo(goodsBefore.getQuantity(GoodsType.MUSKETS) - 50);
        
        assertThat(brave).isUnitRole("model.role.armedBrave");
    }
    
    
    @Test
    public void indianSlaughterUnitInColonyBuilding() throws Exception {
        // given
        Unit brave = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById("model.unit.brave"),
            Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
            aztec
        );
        Tile freeTileNextToColony = game.map.getSafeTile(21, 80);
        brave.changeUnitLocation(freeTileNextToColony);
        
        Tile emptyColonyTile = game.map.getSafeTile(20, 79);
        Colony colony = emptyColonyTile.getSettlement().getColony();

        // when
        combat.init(game, brave, emptyColonyTile);
        combat.combatSides.defender = dutch.units.getById("unit:6652"); // unit in building
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(1.5f, 2.25f, 0.4f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.SLAUGHTER_UNIT);
        
        PlayerAssert.assertThat(dutch).notContainsUnit(combat.combatSides.loser);
        ColonyAssert.assertThat(colony).notContainsUnit(combat.combatSides.loser);
    }
    
    @Test
    public void indianSlaughterUnitInColonyTile() throws Exception {
        // given
        Unit brave = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById("model.unit.brave"),
            Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
            aztec
        );
        Tile freeTileNextToColony = game.map.getSafeTile(21, 80);
        brave.changeUnitLocation(freeTileNextToColony);
        
        Tile emptyColonyTile = game.map.getSafeTile(20, 79);
        Colony colony = emptyColonyTile.getSettlement().getColony();

        // when
        combat.init(game, brave, emptyColonyTile);
        combat.combatSides.defender = dutch.units.getById("unit:6766"); // unit in building
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(1.5f, 2.25f, 0.4f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.SLAUGHTER_UNIT);
        
        PlayerAssert.assertThat(dutch).notContainsUnit(combat.combatSides.loser);
        ColonyAssert.assertThat(colony).notContainsUnit(combat.combatSides.loser);
    }
    
    @Test
    public void indianPillageColony() throws Exception {
        // given
        Unit brave = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById("model.unit.brave"),
            Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
            aztec
        );
        Tile freeTileNextToColony = game.map.getSafeTile(21, 80);
        brave.changeUnitLocation(freeTileNextToColony);
        
        Tile emptyColonyTile = game.map.getSafeTile(20, 79);
        Colony colony = emptyColonyTile.getSettlement().getColony();
        Unit frigate = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById("model.unit.frigate"), 
            Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID), 
            colony.getOwner()
        );
        frigate.changeUnitLocation(emptyColonyTile);

        int colonyOwnerNotificationsSize = colony.getOwner().eventsNotifications.getNotifications().size();
        int spanishNotificationSize = spanish.eventsNotifications.getNotifications().size();
        
        // when
        combat.init(game, brave, emptyColonyTile);
        combat.generateOrdinaryWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(1.5f, 2.25f, 0.4f)
            .hasResult(CombatResult.WIN, false)
            .hasDetails(CombatResultDetails.PILLAGE_COLONY);

        PlayerAssert.assertThat(colony.getOwner())
        	.hasNotificationSize(colonyOwnerNotificationsSize + 1);
        PlayerAssert.assertThat(spanish)
    		.hasNotificationSize(spanishNotificationSize + 1);
    }
    
    @Test 
	public void damageColonyBuildingRemoveUnitsToLowerLevel() throws Exception {
		// given
    	Tile newAmsterdamTile = game.map.getSafeTile(24, 78);
    	Colony newAmsterdam = newAmsterdamTile.getSettlement().getColony();

    	Building lumberMill = newAmsterdam.findBuildingByType("model.building.lumberMill");
    	Unit carpenter = lumberMill.getUnits().first();
    	
		// when
    	newAmsterdam.damageBuilding(lumberMill);

		// then
    	ColonyAssert.assertThat(newAmsterdam)
    		.hasNotBuilding("model.building.lumberMill")
    		.hasBuilding("model.building.carpenterHouse");
    	Building carpenterHouse = newAmsterdam.findBuildingByType("model.building.carpenterHouse");
    	MapIdEntitiesAssert.assertThat(carpenterHouse.getUnits())
    		.containsId(carpenter);
    	
    	UnitAssert.assertThat(carpenter)
    		.isAtLocation(Building.class)
    		.isAtLocation(newAmsterdam.findBuildingByType("model.building.carpenterHouse"));
	}

    @Test 
	public void damageColonyBuildingRemoveUnitsFromIt() throws Exception {
		// given
    	Tile newAmsterdamTile = game.map.getSafeTile(24, 78);
    	Colony newAmsterdam = newAmsterdamTile.getSettlement().getColony();
    	Unit carpenter = newAmsterdam.getOwner().units.getById("unit:6940");

    	Building schoolhouse = newAmsterdam.addBuilding(
			Specification.instance.buildingTypes.getById("model.building.schoolhouse")
		);
    	carpenter.changeUnitLocation(schoolhouse);
    	
    	UnitAssert.assertThat(carpenter)
    		.isAtLocation(Building.class)
    		.isAtLocation(schoolhouse);
    	
		// when
    	newAmsterdam.damageBuilding(schoolhouse);

		// then
    	ColonyAssert.assertThat(newAmsterdam)
    		.hasNotBuilding("model.building.schoolhouse");
    	UnitAssert.assertThat(carpenter)
			.isAtLocation(Building.class)
			.isNotAtLocation(schoolhouse);
	}
    
    @Test 
	public void damageColonyBuildingCanRemoveUnitsFromWorkingLocation() throws Exception {
		// given
    	Tile newAmsterdamTile = game.map.getSafeTile(24, 78);
    	Colony newAmsterdam = newAmsterdamTile.getSettlement().getColony();
    	Unit fisherman = newAmsterdam.getOwner().units.getById("unit:7096");

    	Building docks = newAmsterdam.findBuildingByType("model.building.docks");
    	UnitAssert.assertThat(fisherman)
			.isAtLocation(ColonyTile.class);
    	
		// when
    	newAmsterdam.damageBuilding(docks);
		
		// then
    	ColonyAssert.assertThat(newAmsterdam)
    		.hasNotBuilding("model.building.docks");
    	UnitAssert.assertThat(fisherman)
    		.isAtLocation(Building.class);
	}
    
    @Test 
	public void indianDestroyColony() throws Exception {
		// given
    	Tile trinidadTile = game.map.getSafeTile(27, 57);
    	Colony trinidadColony = trinidadTile.getSettlement().getColony();
    	
    	Tile braveTile = game.map.getSafeTile(27, 55);
    	Unit brave = braveTile.getUnits().first();
    	Unit colonyUnit = spanish.units.getById("unit:6447");
    	
		// when
        combat.init(game, brave, trinidadTile);
        combat.generateGreatWin();
        combat.processAttackResult();

		// then
        assertThat(combat)
	        .hasPowers(1.5f, 2.25f, 0.4f)
	        .hasResult(CombatResult.WIN, true)
	        .hasDetails(CombatResultDetails.SLAUGHTER_UNIT, CombatResultDetails.DESTROY_COLONY);
        
        UnitAssert.assertThat(colonyUnit)
        	.isDisposed();
        ColonyAssert.assertThat(trinidadColony)
        	.notContainsUnit(colonyUnit);
        PlayerAssert.assertThat(spanish)
            .hasNotColony(trinidadColony);
        TileAssert.assertThat(trinidadTile)
            .hasNotSettlement();
	}
}
