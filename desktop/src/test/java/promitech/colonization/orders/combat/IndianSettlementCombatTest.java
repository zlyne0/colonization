package promitech.colonization.orders.combat;

import static org.assertj.core.api.Assertions.*;
import static promitech.colonization.orders.combat.CombatAssert.assertThat;

import java.util.Arrays;
import java.util.Locale;

import org.junit.After;
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
import promitech.colonization.MockedRandomizer;
import promitech.colonization.Randomizer;
import promitech.colonization.orders.combat.Combat.CombatResult;
import promitech.colonization.orders.combat.Combat.CombatResultDetails;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.ui.resources.Messages;

public class IndianSettlementCombatTest {

    private Game game;
    private Player dutch;
    private Player indian;
    private Combat combat = new Combat();
    
    Unit dutchDragoon;
    Tile freeTileNextToIndianSettlement;
    Tile indianSettlementTile;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
        Locale.setDefault(Locale.US);
        Messages.instance().load();
    }

    @Before
    public void setup() throws Exception {
        Randomizer.changeRandomToRandomXS128();
        
        game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
        dutch = game.players.getById("player:1");
        indian = game.players.getById("player:154");
        
        dutchDragoon = new Unit(
            Game.idGenerator.nextId(Unit.class), 
            Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST),
            Specification.instance.unitRoles.getById(UnitRole.DRAGOON),
            dutch
        );
        freeTileNextToIndianSettlement = game.map.getSafeTile(20, 78);
        dutchDragoon.changeUnitLocation(freeTileNextToIndianSettlement);
        
        indianSettlementTile = game.map.getSafeTile(19, 78);
    }

    @After
    public void after() {
        Randomizer.changeRandomToRandomXS128();
    }
    
    @Test
    public void dragoonSuccessfullyAttackSettlement() throws Exception {
        // given
        Randomizer.changeRandomObject(new MockedRandomizer().withFloatsResults(1, 1, 1, 1 ));

        // when
        combat.init(game, dutchDragoon, indianSettlementTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(4.5f, 3.0f, 0.6f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.SLAUGHTER_UNIT, CombatResultDetails.PROMOTE_UNIT);
        fail("assert results");
    }
    
    @Test
    public void dragoonSuccessfullyAttackSettlementAndCaptureConvert() throws Exception {
        // given
        Randomizer.changeRandomObject(new MockedRandomizer().withFloatsResults(0, 1, 1, 1 ));

        // when
        combat.init(game, dutchDragoon, indianSettlementTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(4.5f, 3.0f, 0.6f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.SLAUGHTER_UNIT, CombatResultDetails.CAPTURE_CONVERT, CombatResultDetails.PROMOTE_UNIT);
        fail("");
    }
    
    @Test
    public void dragoonSuccessfullyAttackSettlementAndBurnMission() throws Exception {
        // given
        Randomizer.changeRandomObject(new MockedRandomizer().withFloatsResults(1, 0, 1, 1 ));

        // when
        combat.init(game, dutchDragoon, indianSettlementTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(4.5f, 3.0f, 0.6f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.SLAUGHTER_UNIT, CombatResultDetails.BURN_MISSIONS, CombatResultDetails.PROMOTE_UNIT);
        fail("");
    }
    
    @Test
    public void dragoonSuccessfullyAttackSettlementAndDestroySettlement() throws Exception {
        // given

        // when

        // then
        fail("");
    }
 
    @Test
    public void dragoonFailedAttack() throws Exception {
        // given

        // when

        // then
        fail("");
    }
    
}
