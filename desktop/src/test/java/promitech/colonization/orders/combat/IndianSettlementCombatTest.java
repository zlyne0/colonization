package promitech.colonization.orders.combat;

import static net.sf.freecol.common.model.UnitAssert.assertThat;
import static org.assertj.core.api.Assertions.*;
import static promitech.colonization.orders.combat.CombatAssert.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.IndianSettlementAssert;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.MapIdEntitiesAssert;
import net.sf.freecol.common.model.SettlementPlunderRange;
import net.sf.freecol.common.model.SettlementType;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileAssert;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitAssert;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.PlayerAssert;
import net.sf.freecol.common.model.specification.NationType;
import net.sf.freecol.common.model.specification.RandomRangeAssert;
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
    
    @BeforeAll
    public static void beforeClass() throws Exception {
        Gdx.files = new Lwjgl3Files();
        Locale.setDefault(Locale.US);
        Messages.instance().load();
    }

    @BeforeEach
    public void setup() throws Exception {
        Randomizer.changeRandomToRandomXS128();
        
        game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
        dutch = game.players.getById("player:1");
        indian = game.players.getById("player:154");
        
        freeTileNextToIndianSettlement = game.map.getSafeTile(20, 78);
        dutchDragoon = UnitFactory.createDragoon(dutch, freeTileNextToIndianSettlement);

        
        indianSettlementTile = game.map.getSafeTile(19, 78);
        indianSettlement = indianSettlementTile.getSettlement().asIndianSettlement();
        
        settlementUnits = unitsFromIndianSettlement();
    }

    @AfterEach
    public void after() {
        Randomizer.changeRandomToRandomXS128();
    }
    
    private MapIdEntities<Unit> unitsFromIndianSettlement() {
        MapIdEntities<Unit> settlementUnits = new MapIdEntities<>();
        settlementUnits.addAll(indianSettlementTile.getUnits());
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
    	MapIdEntities<Unit> units = unitsFromIndianSettlement();
    	Unit oneUnit = units.getById("unit:6351");
    	for (Unit u : units.entities()) {
    		if (u.equalsId(oneUnit)) {
    			continue;
    		}
    		u.removeFromLocation();
    	}

        // when
        combat.init(game, dutchDragoon, indianSettlementTile);
        combat.generateGreatWin();
        combat.processAttackResult();

        // then
        assertThat(combat)
	        .hasPowers(4.5f, 3.0f, 0.6f)
	        .hasResult(CombatResult.WIN, true)
	        .hasDetails(CombatResultDetails.SLAUGHTER_UNIT, CombatResultDetails.DESTROY_SETTLEMENT, CombatResultDetails.PROMOTE_UNIT);
        
        verifyTreasureUnit();
        
        IndianSettlementAssert.assertThat(indianSettlement)
        	.notOwnedBy(indian);
        TileAssert.assertThat(indianSettlementTile)
        	.hasNotSettlement();
    }
 
    private void verifyTreasureUnit() {
    	Unit treasureUnit = null;
    	for (Unit unit : indianSettlementTile.getUnits().entities()) {
			if (unit.canCarryTreasure()) {
				treasureUnit = unit;
			}
		}
    	UnitAssert.assertThat(treasureUnit)
			.isNotNull()
			.isOwnedBy(dutch);
	}

	@Test
    public void dragoonFailedAttack() throws Exception {
        // given

        // when
        combat.init(game, dutchDragoon, indianSettlementTile);
        combat.generateGreatLoss();
        combat.processAttackResult();

        // then
        assertThat(combat)
	        .hasPowers(4.5f, 3.0f, 0.6f)
	        .hasResult(CombatResult.LOSE, true)
	        .hasDetails(CombatResultDetails.CAPTURE_EQUIP);
        
        assertThat(dutchDragoon).isUnitRole(UnitRole.SOLDIER);
        assertThat(combat.combatSides.defender).isUnitRole("model.role.mountedBrave");
    }

    @Test 
	public void canDeterminePlunderRangeWithoutHernanCortes() throws Exception {
		// given
    	dutch.foundingFathers.removeId("model.foundingFather.hernanCortes");
    	
    	NationType inca = Specification.instance.nationTypes.getById("model.nationType.inca");
    	SettlementType incaSettlementType = inca.settlementTypes.getById("model.settlement.inca");
    	
		// when
    	SettlementPlunderRange range = incaSettlementType.plunderGoldRange(dutchDragoon);

		// then
    	RandomRangeAssert.assertThat(range)
    		.equalsProbMinMaxFactor(100, 2, 6, 2100);
	}

    @Test 
	public void canDeterminePlunderRangeWithHernanCortes() throws Exception {
		// given
    	FoundingFather cortes = Specification.instance.foundingFathers.getById("model.foundingFather.hernanCortes");
    	dutch.addFoundingFathers(game, cortes);
    	
    	NationType inca = Specification.instance.nationTypes.getById("model.nationType.inca");
    	SettlementType incaSettlementType = inca.settlementTypes.getById("model.settlement.inca");

    	// when
    	SettlementPlunderRange range = incaSettlementType.plunderGoldRange(dutchDragoon);

		// then
    	RandomRangeAssert.assertThat(range)
    		.equalsProbMinMaxFactor(100, 2, 6, 3100);
	}
    
}
