package promitech.colonization.orders.combat;

import static net.sf.freecol.common.model.UnitAssert.assertThat;
import static promitech.colonization.orders.combat.CombatAssert.assertThat;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.orders.combat.Combat;
import promitech.colonization.orders.combat.Combat.CombatResult;
import promitech.colonization.orders.combat.Combat.CombatResultDetails;
import promitech.colonization.savegame.SaveGameParser;

public class LandVsLandCombatTest {

	private Game game;
    private Player spanish;
    private Player dutch;
    private Tile freeTile;
	
    @BeforeClass
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
    }

    @Before
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	spanish = game.players.getById("player:133"); 
    	dutch = game.players.getById("player:1");
    	freeTile = game.map.getSafeTile(23, 80);    	
    }
    
    @Test
    public void dragoonWinPromoteAndKillBrave() throws Exception {
        // given
        Unit dragoon = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
            Specification.instance.unitRoles.getById(UnitRole.DRAGOON),
            dutch
        );
        
        Tile braveTile = game.map.getSafeTile(22, 79);
        Unit braveUnit = braveTile.getUnits().getById("unit:5967");
        
        // when
        Combat combat = new Combat();
        combat.init(game, dragoon, braveTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(1.12f, 1.25f, 0.47f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.SLAUGHTER_UNIT, CombatResultDetails.PROMOTE_UNIT);

        assertThat(braveUnit).isDisposed();
        assertThat(dragoon).isUnitType(UnitType.VETERAN_SOLDIER);
    }
    
    @Test
    public void braveWinCaptureEquipment() throws Exception {
        // given
        Unit dragoon = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
            Specification.instance.unitRoles.getById(UnitRole.DRAGOON),
            dutch
        );
        Tile dragoonTile = game.map.getSafeTile(23, 78);
        dragoon.changeUnitLocation(dragoonTile);
        
        Tile braveTile = game.map.getSafeTile(22, 79);
        Unit braveUnit = braveTile.getUnits().getById("unit:5967");

        // when
        Combat combat = new Combat();
        combat.init(game, braveUnit, dragoonTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(1.5f, 3.75f, 0.28f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.CAPTURE_EQUIP);
        
        assertThat(dragoon).isUnitRole(UnitRole.SOLDIER);
        assertThat(braveUnit).isUnitRole("model.role.mountedBrave");
    }
    
    @Test
    public void mountedBraveWinAndCaptureMuskets() throws Exception {
        // given
        Unit soldier = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
            Specification.instance.unitRoles.getById(UnitRole.SOLDIER),
            dutch
        );
        Tile soldierTile = game.map.getSafeTile(23, 78);
        soldier.changeUnitLocation(soldierTile);
        
        Tile mountedBraveTile = game.map.getSafeTile(22, 79);
        Unit mountedBraveUnit = mountedBraveTile.getUnits().getById("unit:5967");
        mountedBraveUnit.changeRole(Specification.instance.unitRoles.getById("model.role.mountedBrave"));

        // when
        Combat combat = new Combat();
        combat.init(game, mountedBraveUnit, soldierTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(3.0f, 2.5f, 0.54f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.CAPTURE_EQUIP);
        
        assertThat(soldier).isUnitRole(UnitRole.DEFAULT_ROLE_ID);
        assertThat(mountedBraveUnit).isUnitRole("model.role.nativeDragoon");
    }
    
    @Test
    public void artilleryWinVsDragoon() throws Exception {
        Unit dragoon = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
            Specification.instance.unitRoles.getById(UnitRole.DRAGOON),
            dutch
        );
        
        Unit artillery = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById("model.unit.artillery"),
            Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
            spanish
        );
        Tile artilleryTile = freeTile;
        artillery.changeUnitLocation(artilleryTile);
        
        
        // when
        Combat combat = new Combat();
        combat.init(game, dragoon, artilleryTile);
        combat.generateGreatLoss();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(1.12f, 1.25f, 0.47f)
            .hasResult(CombatResult.LOSE, true)
            .hasDetails(CombatResultDetails.LOSE_EQUIP);
        
        assertThat(dragoon).isUnitRole(UnitRole.SOLDIER);
    }
    
    @Test
    public void demoteArtillery() throws Exception {
        // given
        Unit dragoon = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
            Specification.instance.unitRoles.getById(UnitRole.DRAGOON),
            dutch
        );
        Tile dragoonTile = game.map.getSafeTile(23, 78);
        dragoon.changeUnitLocation(dragoonTile);

        Unit artillery = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById("model.unit.artillery"),
            Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
            spanish
        );
        Tile artilleryTile = freeTile;
        artillery.changeUnitLocation(artilleryTile);
        
        // when
        Combat combat = new Combat();
        combat.init(game, dragoon, artilleryTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(4.5f, 1.25f, 0.78f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.DEMOTE_UNIT, CombatResultDetails.PROMOTE_UNIT);
        
        assertThat(artillery).isUnitType("model.unit.damagedArtillery");
    }
    
    @Test
    public void canCaptureUnit() throws Exception {
        // given
        Unit dragoon = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
            Specification.instance.unitRoles.getById(UnitRole.DRAGOON),
            dutch
        );
        Tile dragoonTile = game.map.getSafeTile(23, 78);
        dragoon.changeUnitLocation(dragoonTile);
        
        Unit colonist = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
            Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
            spanish
        );
        Tile colonistTile = freeTile;
        colonist.changeUnitLocation(colonistTile);
        

        // when
        Combat combat = new Combat();
        combat.init(game, dragoon, colonistTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(4.5f, 1.0f, 0.81f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.CAPTURE_UNIT, CombatResultDetails.PROMOTE_UNIT);

        assertThat(colonist)
            .isExistsOnTile(dragoonTile)
            .isOwnedBy(dutch);
    }
}
