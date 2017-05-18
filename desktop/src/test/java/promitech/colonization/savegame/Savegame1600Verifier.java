package promitech.colonization.savegame;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ResourceType;
import net.sf.freecol.common.model.SettlementType;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Specification.Options;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileType;
import net.sf.freecol.common.model.TileTypeTransformation;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.map.generator.MapGeneratorOptions;
import net.sf.freecol.common.model.player.ArmyForceAbstractUnit;
import net.sf.freecol.common.model.player.Monarch.MonarchAction;
import net.sf.freecol.common.model.player.MonarchActionNotification;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Stance;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.EuropeanNationType;
import net.sf.freecol.common.model.specification.FoundingFather;
import net.sf.freecol.common.model.specification.FoundingFather.FoundingFatherType;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.NationType;
import net.sf.freecol.common.model.specification.RequiredGoods;
import net.sf.freecol.common.model.specification.options.OptionGroup;

public class Savegame1600Verifier {

	public void verify(Game game) {
        verifySpecification(game);
        
        verifyGame(game);
        verifyPlayers(game);
        
        Tile tile = game.map.getTile(31, 23);
        Unit tileUnit = tile.getUnits().getById("unit:6449");
        assertNotNull(tileUnit);
        
        Unit unit = tileUnit.getUnitContainer().getUnits().first();
        assertNotNull(unit);
        assertEquals("unit:7049", unit.getId());
        
        verifyPlayer(game);
        verifySettlementsGoods(game);
        verifySettlementsBuildings(game);
        
        verifySettlementBuildingWorker(game);
	}

	private void verifyGame(Game game) {
		assertEquals("unit:6781", game.activeUnitId);
		
		assertThat(game.getCitiesOfCibola()).hasSize(6);
		for (int i=0; i<6; i++) {
			assertThat(game.getCitiesOfCibola()).contains("lostCityRumour.cityName." + i);
		}
	}
	
	private void verifyPlayers(Game game) {
        assertEquals(12, game.players.size());
        for (Player player : game.players.entities()) {
        	if (player.isColonial()) {
        		assertThat(player.getEurope().hasOwner()).isTrue();
        	}
        }
	}

	private void verifyPlayer(Game game) {
		Player player = game.players.getById("player:1");

        assertEquals(Stance.WAR, player.getStance(game.players.getById("player:133")));
		
        assertNotNull(player.getEurope());
        
        assertNotNull(player.getEurope().getUnits().getById("unit:7108"));
        assertNotNull(player.getEurope().getUnits().getById("unit:7109"));
        assertNotNull(player.getEurope().getUnits().getById("unit:7097"));
        assertNotNull(player.getEurope().getUnits().getById("unit:7095"));
        assertNotNull(player.getHighSeas().getUnits().getById("unit:6437"));
        
        assertEquals(16, player.market().marketGoods.size());
        Object food = player.market().marketGoods.getById("model.goods.food");
        assertNotNull(food);
        
        assertEquals(2, player.foundingFathers.size());
        assertNotNull(player.foundingFathers.getById("model.foundingFather.peterMinuit"));
        assertNotNull(player.foundingFathers.getById("model.foundingFather.williamBrewster"));
        
        assertEquals(3, player.eventsNotifications.getNotifications().size());
        
        MonarchActionNotification monarchNotification = (MonarchActionNotification)player.eventsNotifications.getNotifications().get(1);
        assertEquals(MonarchAction.RAISE_TAX_ACT, monarchNotification.getAction());
        assertEquals("model.goods.furs", monarchNotification.getGoodsType().getId());
        assertEquals(12, monarchNotification.getTax());
        
        MonarchActionNotification man1236 = (MonarchActionNotification)player.eventsNotifications.getNotifications().get(2);
        assertEquals("monarchActionNotification:1236", man1236.getId());
        assertEquals(MonarchAction.MONARCH_MERCENARIES, man1236.getAction());
        assertEquals(1500, man1236.getPrice());
        assertEquals(3, man1236.getMercenaries().size());
        
        verifyPlayerMonarch(player);
	}

	private void verifyPlayerMonarch(Player player) {
		assertNotNull(player.getMonarch());
        ArmyForceAbstractUnit manOfWar = player.getMonarch().getExpeditionaryForce().navalUnits.getById("model.role.default");
        assertEquals(13, manOfWar.getAmount());
        assertEquals("model.role.default", manOfWar.getUnitRole().getId());
        assertEquals("model.unit.manOWar", manOfWar.getUnitType().getId());
        
        ArmyForceAbstractUnit cavalery = player.getMonarch().getExpeditionaryForce().landUnits.getById("model.role.cavalry");
        assertEquals(15, cavalery.getAmount());
        assertEquals("model.role.cavalry", cavalery.getUnitRole().getId());
        assertEquals("model.unit.kingsRegular", cavalery.getUnitType().getId());
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
		
		verifyOptions(Specification.options);
		
        assertEquals(42, Specification.instance.unitTypes.size());
        verifyTileTypes(specification);

        assertEquals(12, specification.resourceTypes.size());
        
        verifyTileImprovementTypes(specification);
        
        assertEquals(11, specification.unitRoles.size());
        assertEquals(25, specification.nations.size());
        assertEquals(21, specification.goodsTypes.size());
        
        verifyNationTypes(specification);
        verifySpecificationBuildType(specification);
        verifyShipGoods(game, specification);
        verifySpecificationOptionGroup(specification);
        verifySpecificationGameDifficultyOptions(specification);
        verifySpecificationUnitRoles(specification);
        verifySpecificationUnitTypes(specification);
        verifySpecificationFoundingFathers(specification);
    }

	private void verifyOptions(Options options) {
		int temperature = options.getIntValue(MapGeneratorOptions.TEMPERATURE);
		assertEquals(2, temperature);
	}

	private void verifyTileTypes(Specification specification) {
		assertEquals(23, specification.tileTypes.size());
		
		TileType mixedForest = specification.tileTypes.getById("model.tile.mixedForest");
		assertEquals(2, mixedForest.allowedResourceTypes.size());
		
		ResourceType furs = specification.resourceTypes.getById("model.resource.furs");
		ResourceType lumber = specification.resourceTypes.getById("model.resource.lumber");
		assertTrue(mixedForest.canHaveResourceType(furs));
		assertTrue(mixedForest.canHaveResourceType(lumber));
	}

    private void verifyTileImprovementTypes(Specification specification) {
        assertEquals( 6, specification.tileImprovementTypes.size());
        TileImprovementType clearForest = specification.tileImprovementTypes.getById("model.improvement.clearForest");
        assertEquals( 8, clearForest.getTileTypeTransformation().size());
        
        TileType borealForest = specification.tileTypes.getById("model.tile.borealForest");
        TileTypeTransformation changedTileType = clearForest.changedTileType(borealForest);
        assertEquals("model.tile.tundra", changedTileType.getToType().getId());
        
    	assertEquals("model.goods.lumber", changedTileType.getProduction().getId());
    	assertEquals(20, changedTileType.getProduction().getAmount());
	}

	private void verifySpecificationFoundingFathers(Specification specification) {
        assertEquals(25, specification.foundingFathers.size());
        
        FoundingFather henryHudson = specification.foundingFathers.getById("model.foundingFather.henryHudson");
        assertEquals(FoundingFatherType.EXPLORATION, henryHudson.getType());
        assertTrue(henryHudson.hasModifier("model.goods.furs"));

        FoundingFather adamSmith = specification.foundingFathers.getById("model.foundingFather.adamSmith");
        assertEquals(FoundingFatherType.TRADE, adamSmith.getType());
        assertTrue(adamSmith.hasAbility("model.ability.buildFactory"));

        FoundingFather pocahontas = specification.foundingFathers.getById("model.foundingFather.pocahontas");
        assertEquals(FoundingFatherType.POLITICAL, pocahontas.getType());
        assertNotNull(pocahontas.events.getById("model.event.resetNativeAlarm"));
        assertNotNull(pocahontas.events.getById("model.event.resetBannedMissions"));
    }

	private void verifyNationTypes(Specification specification) {
		assertEquals(18, specification.nationTypes.size());
        NationType arawakNationType = specification.nationTypes.getById("model.nationType.arawak");
        SettlementType settlementType = arawakNationType.settlementTypes.getById("model.settlement.village");
        assertNotNull(settlementType);
        
        NationType tradeNationType = specification.nationTypes.getById("model.nationType.trade");
        assertTrue(tradeNationType.hasModifier("model.modifier.tradeBonus"));
        assertTrue(tradeNationType.hasAbility("model.ability.electFoundingFather"));
        
        EuropeanNationType buildingNationType = (EuropeanNationType)specification.nationTypes.getById("model.nationType.building");
        assertEquals(3, buildingNationType.getStartedUnits(true).size());
	}

    private void verifySpecificationUnitTypes(Specification specification) {
    	UnitType unitType = specification.unitTypes.getById("model.unit.flyingDutchman");
    	assertEquals(1, unitType.requiredAbilitiesAmount());
    	
    	UnitType caravel = specification.unitTypes.getById("model.unit.caravel");
    	assertThat(caravel.getPrice()).isEqualTo(1000);
    	assertThat(caravel.hasAbility(Ability.NAVAL_UNIT)).isTrue();
    	assertThat(caravel.applyModifier(Modifier.TRADE_VOLUME_PENALTY, 100)).isEqualTo(25);
    	
    	UnitType freeColonist = specification.unitTypes.getById(UnitType.FREE_COLONIST);
    	assertThat(freeColonist.unitConsumption.getById(GoodsType.FOOD).getQuantity()).isEqualTo(2);
    	assertThat(freeColonist.unitConsumption.getById(GoodsType.BELLS).getQuantity()).isEqualTo(1);
	}

	private void verifySpecificationUnitRoles(Specification specification) {
        UnitRole dragoonUnitRole = specification.unitRoles.getById("model.role.dragoon");
        
        assertEquals(5, dragoonUnitRole.abilitiesAmount());
        assertEquals(3, dragoonUnitRole.modifiersAmount());
        
        assertEquals(3, dragoonUnitRole.requiredAbilitiesAmount());
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
    	
        assertThat(Specification.options.getIntValue(GameOptions.BAD_RUMOUR)).isEqualTo(23);
        assertThat(Specification.options.getIntValue(GameOptions.GOOD_RUMOUR)).isEqualTo(48);
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
        	assertEquals(1, furFactory.requiredAbilitiesAmount());
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
