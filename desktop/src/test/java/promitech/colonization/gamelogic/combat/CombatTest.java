package promitech.colonization.gamelogic.combat;

import static promitech.colonization.gamelogic.combat.CombatAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import static net.sf.freecol.common.model.UnitAssert.assertThat;

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
import net.sf.freecol.common.model.ColonyAssert;
import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.PlayerAssert;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileAssert;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitAssert;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.Unit.UnitState;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.FoundingFather;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.gamelogic.combat.Combat.CombatResult;
import promitech.colonization.gamelogic.combat.Combat.CombatResultDetails;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.resources.Messages;

public class CombatTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	private Game game;
    private Player spanish;
    private Player dutch;
    private Player aztec;
	
    @BeforeClass
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
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
        Combat combat = new Combat();
        combat.init(dragoon, colonyTile);
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
        Combat combat = new Combat();
        combat.init(dragoon, emptyColonyTile);
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
        Combat combat = new Combat();
        combat.init(dragoon, emptyColonyTile);
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
        Combat combat = new Combat();
        combat.init(dragoon, emptyColonyTile);
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
        Combat combat = new Combat();
        combat.init(brave, emptyColonyTile);
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
        Combat combat = new Combat();
        combat.init(brave, emptyColonyTile);
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
        Combat combat = new Combat();
        combat.init(brave, emptyColonyTile);
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
        Combat combat = new Combat();
        combat.init(brave, emptyColonyTile);
        combat.combatSides.defender = dutch.units.getById("unit:6652"); // unit in building
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(1.5f, 2.25f, 0.4f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.SLAUGHTER_UNIT);
        
        PlayerAssert.assertThat(dutch).notContainsUnit(combat.combatResolver.loser);
        ColonyAssert.assertThat(colony).notContainsUnit(combat.combatResolver.loser);
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
        Combat combat = new Combat();
        combat.init(brave, emptyColonyTile);
        combat.combatSides.defender = dutch.units.getById("unit:6766"); // unit in building
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(1.5f, 2.25f, 0.4f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.SLAUGHTER_UNIT);
        
        PlayerAssert.assertThat(dutch).notContainsUnit(combat.combatResolver.loser);
        ColonyAssert.assertThat(colony).notContainsUnit(combat.combatResolver.loser);
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

        // when
        Combat combat = new Combat();
        combat.init(brave, emptyColonyTile);
        combat.generateOrdinaryWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(1.5f, 2.25f, 0.4f)
            .hasResult(CombatResult.WIN, false)
            .hasDetails(CombatResultDetails.PILLAGE_COLONY);

        fail("test not implemented");
    }
    
    @Test 
	public void indianDestroyColony() throws Exception {
		// given

		// when
		

		// then
	}
    
//  <tile id="tile:3391" x="23" y="78" type="model.tile.swamp" style="0" moveToEurope="false" owner="player:1">
//  <cachedTile player="player:1"/>
//  <tileitemcontainer>
//      <tileimprovement id="tileimprovement:6820" type="model.improvement.road" style="10100010" magnitude="1" turns="0"/>
//  </tileitemcontainer>
//  <unit id="unit:6764" unitType="model.unit.freeColonist" role="model.role.dragoon" owner="player:1" state="ACTIVE" movesLeft="11" hitPoints="0" visibleGoodsCount="-1" treasureAmount="0" roleCount="1" experience="0" workLeft="-1"/>
//</tile>
  
//<tile id="tile:3430" x="22" y="79" type="model.tile.marsh" style="0" moveToEurope="false">
//  <cachedTile player="player:1"/>
//  <unit id="unit:5967" unitType="model.unit.brave" role="model.role.default" owner="player:40" state="ACTIVE" movesLeft="3" hitPoints="0" visibleGoodsCount="-1" treasureAmount="0" roleCount="0" experience="0" indianSettlement="indianSettlement:5961" workLeft="-1">
//      <goodsContainer/>
//  </unit>
//</tile>
//wolne pole x="23" y="80"        
  
    
}
