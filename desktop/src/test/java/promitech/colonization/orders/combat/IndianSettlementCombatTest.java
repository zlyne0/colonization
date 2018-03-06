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
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.IndianSettlementAssert;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.MapIdEntitiesAssert;
import net.sf.freecol.common.model.PlayerAssert;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitAssert;
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
    IndianSettlement indianSettlement;
    
    MapIdEntities<Unit> settlementUnits; 
    
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
        indianSettlement = indianSettlementTile.getSettlement().getIndianSettlement();
        
        settlementUnits = unitsFromIndianSettlement();
    }

    @After
    public void after() {
        Randomizer.changeRandomToRandomXS128();
    }
    
    private MapIdEntities<Unit> unitsFromIndianSettlement() {
        MapIdEntities<Unit> settlementUnits = new MapIdEntities<>(indianSettlementTile.getUnits());
        settlementUnits.addAll(indianSettlement.getUnits());
        return settlementUnits;
    }
    
    @Test
    public void dragoonSuccessfullyAttackSettlement() throws Exception {
        // given
        Randomizer.changeRandomObject(new MockedRandomizer()
            .withFloatsResults(1, 1, 1, 1 )
            .withIntsResults(99, 99, 99, 99)
        );
        
        // when
        combat.init(game, dutchDragoon, indianSettlementTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(4.5f, 3.0f, 0.6f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.SLAUGHTER_UNIT, CombatResultDetails.PROMOTE_UNIT);
        
        verifySettlementSlaughterUnit();
    }
    
    private void verifySettlementSlaughterUnit() {
        MapIdEntities<Unit> settlementUnitsAfterAttack = unitsFromIndianSettlement();
        
        MapIdEntities<Unit> slaughterUnits = settlementUnits.reduceBy(settlementUnitsAfterAttack);
        MapIdEntitiesAssert.assertThat(slaughterUnits).hasSize(1);
        UnitAssert.assertThat(slaughterUnits.first()).isDisposed();
    }

    @Test
    public void dragoonSuccessfullyAttackSettlementAndCaptureConvert() throws Exception {
        // given
        Randomizer.changeRandomObject(new MockedRandomizer()
            .withFloatsResults(0, 1, 1, 1 )
            .withIntsResults(0, 99, 99, 99)
        );

        MapIdEntities<Unit> dragoonTileUnitsBeforeCombat = new MapIdEntities<>(dutchDragoon.getTile().getUnits());
        
        // when
        combat.init(game, dutchDragoon, indianSettlementTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(4.5f, 3.0f, 0.6f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.SLAUGHTER_UNIT, CombatResultDetails.CAPTURE_CONVERT, CombatResultDetails.PROMOTE_UNIT);
        
        MapIdEntities<Unit> convertUnits = new MapIdEntities<>(dutchDragoon.getTile().getUnits())
        	.reduceBy(dragoonTileUnitsBeforeCombat);
        
        MapIdEntitiesAssert.assertThat(convertUnits)
        	.hasSize(1);
        Unit convert = convertUnits.first();
        
        PlayerAssert.assertThat(indian).notContainsUnit(convert);
        PlayerAssert.assertThat(dutch).containsUnit(convert);
    }
    
    @Test
    public void dragoonSuccessfullyAttackSettlementAndBurnMission() throws Exception {
        // given
        Randomizer.changeRandomObject(new MockedRandomizer()
            .withFloatsResults(1, 0, 1, 1 )
            .withIntsResults(99, 0, 99, 99)
        );

        // when
        combat.init(game, dutchDragoon, indianSettlementTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
            .hasPowers(4.5f, 3.0f, 0.6f)
            .hasResult(CombatResult.WIN, true)
            .hasDetails(CombatResultDetails.SLAUGHTER_UNIT, CombatResultDetails.BURN_MISSIONS, CombatResultDetails.PROMOTE_UNIT);
        
        verifySettlementSlaughterUnit();
        IndianSettlementAssert.assertThat(indianSettlement).hasNoMissionary(dutch);
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
