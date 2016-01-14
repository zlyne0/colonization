package promitech.colonization.savegame;

import static org.junit.Assert.*;
import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.SettlementType;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.NationType;
import net.sf.freecol.common.model.specification.RequiredGoods;
import net.sf.freecol.common.model.specification.options.OptionGroup;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

public class SaveGameParserTest {

    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }
    
    @Test
    public void canLoadSaveGame() throws Exception {
        // given
        SaveGameParser saveGameParser = new SaveGameParser("maps/savegame_1600_for_jtests.xml");
        
        // when
        Game game = saveGameParser.parse();

        // then
        verifySpecification(game);
        
        assertEquals(13, game.players.size());
        
        Tile tile = game.map.getTile(31, 23);
        Unit tileUnit = tile.units.getById("unit:6449");
        assertNotNull(tileUnit);
        
        Unit unit = tileUnit.getUnitContainer().getUnits().first();
        assertNotNull(unit);
        assertEquals("unit:7049", unit.getId());
        
        verifyPlayer(game);
        verifySettlementsGoods(game);
        verifySettlementsBuildings(game);
        
        verifySettlementBuildingWorker(game);
    }

	private void verifyPlayer(Game game) {
		Player player = game.players.getById("player:1");
        assertNotNull(player.getEurope());
        assertEquals("europe:2", player.getEurope().getId());
        
        assertEquals(16, player.market().marketGoods.size());
        Object food = player.market().marketGoods.getById("model.goods.food");
        assertNotNull(food);
	}
    
    private void verifySettlementBuildingWorker(Game game) {
    	Tile tile = game.map.getTile(28, 40);
		Colony colony = tile.getSettlement().getColony();
    	assertNotNull(colony);
    	System.out.println("size = " + colony.buildings.size());
    	Building carpenterHouse = colony.buildings.getById("building:7122");
    	assertEquals(1, carpenterHouse.workers.size());
    	Unit worker = carpenterHouse.workers.first();
    	assertEquals("unit:6498", worker.getId());
	}

	private void verifySpecification(Game game) {
		Specification specification = Specification.instance;
        assertEquals(42, Specification.instance.unitTypes.size());
        assertEquals(23, specification.tileTypes.size());

        assertEquals(12, specification.resourceTypes.size());
        assertEquals( 6, specification.tileImprovementTypes.size());
        assertEquals(11, specification.unitRoles.size());
        assertEquals(18, specification.nationTypes.size());
        assertEquals(25, specification.nations.size());
        assertEquals(21, specification.goodsTypes.size());
        
        NationType arawakNationType = specification.nationTypes.getById("model.nationType.arawak");
        SettlementType settlementType = arawakNationType.settlementTypes.getById("model.settlement.village");
        assertNotNull(settlementType);
        
        verifySpecificationBuildType(specification);
        verifyShipGoods(game, specification);
        verifySpecificationOptionGroup(specification);
        verifySpecificationGameDifficultyOptions(specification);
        verifySpecificationUnitRoles(specification);
        verifySpecificationUnitTypes(specification);
    }

    private void verifySpecificationUnitTypes(Specification specification) {
    	UnitType unitType = specification.unitTypes.getById("model.unit.flyingDutchman");
    	assertEquals(1, unitType.requiredAbilities.size());
	}

	private void verifySpecificationUnitRoles(Specification specification) {
        UnitRole dragoonUnitRole = specification.unitRoles.getById("model.role.dragoon");
        
        assertEquals(5, dragoonUnitRole.abilitiesAmount());
        assertEquals(3, dragoonUnitRole.modifiersAmount());
        
        assertEquals(3, dragoonUnitRole.requiredAbilities.size());
        assertEquals(2, dragoonUnitRole.requiredGoods.size());
        assertEquals(50, dragoonUnitRole.requiredGoods.getById("model.goods.muskets").getAmount());
        assertEquals(50, dragoonUnitRole.requiredGoods.getById("model.goods.horses").getAmount());
    }

    private void verifySpecificationGameDifficultyOptions(Specification specification) {
        assertEquals(true, specification.options.getBoolean(GameOptions.AMPHIBIOUS_MOVES));
        assertEquals(true, specification.options.getBoolean(GameOptions.EMPTY_TRADERS));
        
        assertEquals(0, specification.options.getIntValue(GameOptions.STARTING_MONEY));
        assertEquals("medium", specification.options.getStringValue(GameOptions.TILE_PRODUCTION));
        
    }

    private void verifySpecificationOptionGroup(Specification specification) {
        assertEquals(4, specification.optionGroupEntities.size());
        
        OptionGroup difficultyLevels = specification.optionGroupEntities.getById("difficultyLevels");
        assertEquals(6, difficultyLevels.optionsGroup.size());
        OptionGroup veryEasyDifficultyLevel = difficultyLevels.optionsGroup.getById("model.difficulty.veryEasy");
        assertEquals(6, veryEasyDifficultyLevel.optionsGroup.size());
        
        OptionGroup other = veryEasyDifficultyLevel.optionsGroup.getById(GameOptions.DIFFICULTY_OTHER);
        assertNotNull(other);
        
        assertEquals(1000, other.getIntValue(GameOptions.STARTING_MONEY));
        assertEquals("veryHigh", other.getStringValue(GameOptions.TILE_PRODUCTION));
    }

    private void verifyShipGoods(Game game, Specification specification) {
        Player player = game.players.getById("player:1");
        Unit privateerUnit = player.units.getById("unit:6900");
        int goodsAmount = privateerUnit.getGoodsContainer().goodsAmount(specification.goodsTypes.getById("model.goods.tools"));
        assertEquals(100, goodsAmount);
        goodsAmount = privateerUnit.getGoodsContainer().goodsAmount(specification.goodsTypes.getById("model.goods.tradeGoods"));
        assertEquals(100, goodsAmount);
    }

    private void verifySpecificationBuildType(Specification specification) {
        assertEquals(41, specification.buildingTypes.size());
        
        BuildingType fortBuildingType = specification.buildingTypes.getById("model.building.fort");
        
        float baseDefence = 100;
        float fortDefence = fortBuildingType.applyModifier("model.modifier.defence", baseDefence);
        assertEquals(250f, fortDefence, 0.01);
        
        RequiredGoods reqGoods1 = fortBuildingType.requiredGoods.getById("model.goods.hammers");
        RequiredGoods reqGoods2 = fortBuildingType.requiredGoods.getById("model.goods.tools");
        assertEquals(120, reqGoods1.amount);
        assertEquals(100, reqGoods2.amount);
        
        assertEquals(0, fortBuildingType.productionInfo.size());
        
        
        {
        	BuildingType furFactory = specification.buildingTypes.getById("model.building.furFactory");
        	assertEquals(1, furFactory.requiredAbilities.size());
        	assertTrue(furFactory.hasRequiredAbility("model.ability.buildFactory", true));
        }
    }

    private void verifySettlementsGoods(Game game) {
        Tile tile = game.map.getTile(24, 78);
        Colony colony = (Colony)tile.getSettlement();
        assertEquals(10, colony.getGoodsContainer().size());
    }
    
    private void verifySettlementsBuildings(Game game) {
        Tile tile = game.map.getTile(24, 78);
        Colony colony = (Colony)tile.getSettlement();
        assertEquals(12, colony.buildings.size());
        
        Building building = colony.buildings.getById("building:6545");
        assertEquals("model.building.furTraderHouse", building.buildingType.getId());
        assertNotNull(building.workers.getById("unit:6765"));
        assertNotNull(building.workers.getById("unit:6439"));
    }

    
}
