package promitech.colonization.gamelogic.combat;

import static net.sf.freecol.common.model.UnitAssert.assertThat;
import static promitech.colonization.gamelogic.combat.CombatAssert.assertThat;

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
import promitech.colonization.gamelogic.combat.Combat.CombatResult;
import promitech.colonization.gamelogic.combat.Combat.CombatResultDetails;
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
        combat.init(dragoon, braveTile);
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
        combat.init(braveUnit, dragoonTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(1.5f, 3.75f, 0.28f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.CAPTURE_EQUIP);
        
        assertThat(dragoon).isUnitRole(UnitRole.SOLDIER);
        assertThat(braveUnit).isUnitRole("model.role.armedBrave");
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
        combat.init(dragoon, artilleryTile);
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
        combat.init(dragoon, artilleryTile);
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
        combat.init(dragoon, colonistTile);
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