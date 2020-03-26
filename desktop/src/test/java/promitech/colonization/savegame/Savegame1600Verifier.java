package promitech.colonization.savegame;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.IndianSettlementAssert;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ResourceType;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.SettlementPlunderRange;
import net.sf.freecol.common.model.SettlementType;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Specification.Options;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileType;
import net.sf.freecol.common.model.TileTypeTransformation;
import net.sf.freecol.common.model.TradeRouteDefinition;
import net.sf.freecol.common.model.TradeRouteStop;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitRoleChange;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.ai.missions.ExplorerMission;
import net.sf.freecol.common.model.ai.missions.FoundColonyMission;
import net.sf.freecol.common.model.ai.missions.IndianBringGiftMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.RellocationMission;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;
import net.sf.freecol.common.model.ai.missions.WanderMission;
import net.sf.freecol.common.model.map.generator.MapGeneratorOptions;
import net.sf.freecol.common.model.player.ArmyForceAbstractUnit;
import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.player.Monarch.MonarchAction;
import net.sf.freecol.common.model.player.MonarchActionNotification;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Stance;
import net.sf.freecol.common.model.player.FoundingFather.FoundingFatherType;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.EuropeanNationType;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.IndianNationType;
import static net.sf.freecol.common.model.specification.IndianNationTypeAssert.assertThat;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.NationType;
import net.sf.freecol.common.model.specification.RandomRangeAssert;
import net.sf.freecol.common.model.specification.RequiredGoods;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import net.sf.freecol.common.model.specification.options.OptionGroup;
import static net.sf.freecol.common.model.TileAssert.assertThat;
import static net.sf.freecol.common.model.EuropeAssert.assertThat;
import static net.sf.freecol.common.model.player.PlayerAssert.assertThat;
import static promitech.colonization.savegame.AbstractMissionAssert.assertThat;
import static promitech.colonization.savegame.ObjectWithFeaturesAssert.assertThat;
import static net.sf.freecol.common.model.MapIdEntitiesAssert.assertThat;

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
        verifyAIContainer(game);
        verifyIndianSettlement(game);
        verifyIndianMissions(game);
        
        verifyUnitTradeRoute(game);
	}

	private void verifyIndianMissions(Game game) {
		PlayerMissionsContainer missionContainer = game.aiContainer.getMissionContainer("player:154");
		
		IndianBringGiftMission mission = missionContainer.getMission("indianBringGiftMission:123");
		assertThat(mission.getIndianSettlement().getId()).isEqualTo("indianSettlement:6339");
	}

	private void verifyUnitTradeRoute(Game game) {
		Tile tile = game.map.getSafeTile(20, 79);
		Unit u = tile.getUnits().getById("unit:7162");
		assertThat(u.getTradeRoute().getId()).isEqualTo("notExistsButOk:1");
		assertThat(u.getTradeRoute().getNextStopLocationIndex()).isEqualTo(1);
	}

	private void verifyIndianSettlement(Game game) {
		Player player = game.players.getById("player:58");
		Settlement settlement = player.settlements.getById("indianSettlement:6019");
		assertThat(settlement.getUnits().size()).isEqualTo(7);
		
		settlement.getGoodsContainer().hasGoodsQuantity("model.goods.cotton", 150);
		settlement.getGoodsContainer().hasGoodsQuantity("model.goods.food", 217);
		settlement.getGoodsContainer().hasGoodsQuantity("model.goods.furs", 200);
		settlement.getGoodsContainer().hasGoodsQuantity("model.goods.sugar", 200);
		settlement.getGoodsContainer().hasGoodsQuantity("model.goods.tobacco", 191);
		
		IndianSettlementAssert.assertThat(settlement.asIndianSettlement())
			.hasWantedGoods("model.goods.tradeGoods", "model.goods.rum", "model.goods.cigars");
		
		verifyMissionary(game);
	}

	private void verifyMissionary(Game game) {
	    Player player = game.players.getById("player:22");
	    IndianSettlement indianSettlement = player.settlements.getById("indianSettlement:5901")
	            .asIndianSettlement();
	    Player dutch = game.players.getById("player:1");
	    assertThat(indianSettlement.hasMissionary(dutch)).isTrue();
    }

    private void verifyAIContainer(Game game) {
		PlayerMissionsContainer playerMissionsContainer = game.aiContainer.getMissionContainer("player:22");
		assertThat(playerMissionsContainer).isNotNull();
		WanderMission wm = playerMissionsContainer.getMission("wanderMission:1");
		
		assertThat(wm.unit.getId()).isEqualTo("unit:6706");

		verifyTransportUnitMission(game);
		verifyRellocationMission(game);
		verifyFoundColonyMission(game);
		verifyExploreMission(game);
		verifyMissionRecursion(game);
	}

	private void verifyMissionRecursion(Game game) {
        PlayerMissionsContainer missions = game.aiContainer.getMissionContainer("player:1");
		
        AbstractMissionAssert.assertThat(missions.getMission("foundColonyMission:4"))
        	.isType(FoundColonyMission.class)
        	.hasDependMission("rellocationMission:3:1", RellocationMission.class);
        
        AbstractMissionAssert.assertThat(missions.getMission("explorerMission:5"))
        	.isType(ExplorerMission.class)
        	.hasDependMission("explorerMission:5:1", ExplorerMission.class)
        	.hasDependMission("explorerMission:5:2", RellocationMission.class);
        
        assertThat(missions.getMission("explorerMission:5").getDependMissionById("explorerMission:5:2"))
        	.hasDependMission("explorerMission:5:2:1", ExplorerMission.class);
	}

	private void verifyExploreMission(Game game) {
        PlayerMissionsContainer missions = game.aiContainer.getMissionContainer("player:1");
        assertThat(missions).isNotNull();
        ExplorerMission em = missions.getMission("explorerMission:5");
        
        assertThat(em.getId()).isEqualTo("explorerMission:5");
        assertThat(em.unit.getId()).isEqualTo("unit:6437");
    }

    private void verifyFoundColonyMission(Game game) {
        PlayerMissionsContainer missions = game.aiContainer.getMissionContainer("player:1");
        assertThat(missions).isNotNull();
        FoundColonyMission fm = missions.getMission("foundColonyMission:4");
        
        assertThat(fm.getId()).isEqualTo("foundColonyMission:4");
        assertThat(fm.destTile).isEquals(21, 23);
        assertThat(fm.unit.getId()).isEqualTo("unit:7095");
    }

    private void verifyRellocationMission(Game game) {
		PlayerMissionsContainer missions = game.aiContainer.getMissionContainer("player:1");
		assertThat(missions).isNotNull();
		RellocationMission rm = missions.getMission("rellocationMission:3");
		
		assertThat(rm.getId()).isEqualTo("rellocationMission:3");
		assertThat(rm.rellocationDestination).isEquals(20, 22);
		
		assertThat(rm.unit.getId()).isEqualTo("unit:7095");
		assertThat(rm.unitDestination).isEquals(25, 27);

		assertThat(rm.carrier.getId()).isEqualTo("unit:6437");
		assertThat(rm.carrierDestination).isEquals(30, 32);
	}

	private void verifyTransportUnitMission(Game game) {
		PlayerMissionsContainer playerMissions1 = game.aiContainer.getMissionContainer("player:1");
		assertThat(playerMissions1).isNotNull();
		TransportUnitMission tm = playerMissions1.getMission("transportUnitMission:2");
		
		assertThat(tm.dest.equalsCoordinates(10, 12)).isTrue();
		assertThat(tm.carrier.getId()).isEqualTo("unit:6437");
		
		assertThat(tm.units.entities()).hasSize(1);
		assertThat(tm.units.first().getId()).isEqualTo("unit:7095");
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
        
        verifyDutchTradeRoutes(game);
	}

    private void verifyDutchTradeRoutes(Game game) {
        Player dutch = game.players.getById("player:1");
        TradeRouteDefinition tradeRoute = dutch.tradeRoutes.getById("tradeRouteDef:1");
        assertThat(dutch.tradeRoutes.size()).isEqualTo(2);

        assertThat(tradeRoute.getName()).isEqualTo("route1");
        assertThat(tradeRoute.getTradeRouteStops())
            .hasSize(3)
            .extracting(TradeRouteStop::getTradeLocationId)
            .containsExactly("colony:6993", "colony:6554", "colony:6528");
        
        assertThat(tradeRoute.getTradeRouteStops().get(0).getGoodsType())
            .extracting(GoodsType::getId)
            .containsExactly("model.goods.sugar", "model.goods.tobacco");
        assertThat(tradeRoute.getTradeRouteStops().get(1).getGoodsType())
            .extracting(GoodsType::getId)
            .containsExactly("model.goods.sugar", "model.goods.ore");
        assertThat(tradeRoute.getTradeRouteStops().get(2).getGoodsType())
            .extracting(GoodsType::getId)
            .containsExactly("model.goods.food");
    }

	private void verifyPlayer(Game game) {
		Player spanish = game.players.getById("player:133");
		assertThat(spanish.getEurope()).hasUnitPrice(Specification.instance.unitTypes.getById(UnitType.ARTILLERY), 600);
		
		Player player = game.players.getById("player:1");
		assertThat(player.getEurope()).hasUnitPrice(Specification.instance.unitTypes.getById(UnitType.ARTILLERY), 500);

        assertEquals(Stance.WAR, player.getStance(spanish));
		
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
        assertNotNull(player.foundingFathers.containsId("model.foundingFather.peterMinuit"));
        assertNotNull(player.foundingFathers.containsId("model.foundingFather.williamBrewster"));
        
        // should add fathers modifiers to player features
        assertThat(player.getFeatures()).hasModifier(Modifier.LAND_PAYMENT_MODIFIER);
        
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
        assertThat(game.players.getById("player:22")).containsExactlyBanMissions("player:1", "player:2");
        
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
		Colony colony = tile.getSettlement().asColony();
    	assertNotNull(colony);
    	System.out.println("size = " + colony.buildings.size());
    	Building carpenterHouse = colony.buildings.getById("building:7122");
    	assertEquals(1, carpenterHouse.getUnits().size());
    	Unit worker = carpenterHouse.getUnits().first();
    	assertEquals("unit:6498", worker.getId());
	}

	private void verifySpecification(Game game) {
		Specification specification = Specification.instance;
		
		verifySpecificationModifiers(specification);
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
        verifySpecificationUnitRoles(specification, game);
        verifySpecificationUnitTypes(specification, game);
        verifySpecificationFoundingFathers(specification);
        verifySpecificationSettlementType(specification);
    }

	private void verifySpecificationSettlementType(Specification specification) {
		NationType apache = specification.nationTypes.getById("model.nationType.apache");
		SettlementType campSettlementType = apache.settlementTypes.getById("model.settlement.camp");
		RandomRangeAssert.assertThat(campSettlementType.getGift())
			.equalsProbMinMaxFactor(100, 2, 3, 100);
		
		SettlementType capitalSettlementType = apache.settlementTypes.getById("model.settlement.camp.capital");
		RandomRangeAssert.assertThat(capitalSettlementType.getGift())
			.equalsProbMinMaxFactor(100, 3, 6, 200);
	}

	private void verifySpecificationModifiers(Specification spec) {
		assertNotNull(spec.modifiers.getById("model.modifier.smallMovementPenalty"));
		assertNotNull(spec.modifiers.getById("model.modifier.bigMovementPenalty"));
		assertNotNull(spec.modifiers.getById("model.modifier.artilleryInTheOpen"));
		assertNotNull(spec.modifiers.getById("model.modifier.attackBonus"));
		assertNotNull(spec.modifiers.getById("model.modifier.fortified"));
		assertNotNull(spec.modifiers.getById("model.modifier.artilleryAgainstRaid"));
		assertNotNull(spec.modifiers.getById("model.modifier.amphibiousAttack"));
		assertNotNull(spec.modifiers.getById("model.modifier.colonyGoodsParty"));
		assertNotNull(spec.modifiers.getById("model.goods.food"));
		assertNotNull(spec.modifiers.getById("model.modifier.shipTradePenalty"));
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
        
        FoundingFather johnPaulJones = specification.foundingFathers.getById("model.foundingFather.johnPaulJones");
        assertThat(johnPaulJones.getUnitTypes()).containsExactly("model.unit.frigate");
        
        FoundingFather bartolomeDeLasCasas = specification.foundingFathers.getById("model.foundingFather.bartolomeDeLasCasas");
        assertThat(bartolomeDeLasCasas.getUpgrades())
        	.hasSize(1)
        	.containsEntry("model.unit.indianConvert", "model.unit.freeColonist");
    }

	private void verifyNationTypes(Specification specification) {
		assertEquals(18, specification.nationTypes.size());
		IndianNationType arawakNationType = (IndianNationType)specification.nationTypes.getById("model.nationType.arawak");
        
		assertThat(arawakNationType.getSkills()).hasSize(3);
		assertThat(arawakNationType).hasSkill("model.unit.masterSugarPlanter", 20);
		assertThat(arawakNationType).hasSkill("model.unit.expertFisherman", 10);
		assertThat(arawakNationType).hasSkill("model.unit.masterFurTrader", 3);
        
        SettlementType settlementType = arawakNationType.settlementTypes.getById("model.settlement.village");
        assertNotNull(settlementType);
        assertThat(settlementType.getPlunderRanges()).hasSize(2);
        for (SettlementPlunderRange spr : settlementType.getPlunderRanges()) {
            assertThat(spr.getScopes()).hasSize(1);
        }
        
        NationType tradeNationType = specification.nationTypes.getById("model.nationType.trade");
        assertTrue(tradeNationType.hasModifier("model.modifier.tradeBonus"));
        assertTrue(tradeNationType.hasAbility("model.ability.electFoundingFather"));
        SettlementType colonySettlementType = tradeNationType.settlementTypes.getById("model.settlement.colony");
        assertTrue(colonySettlementType.hasModifier(Modifier.DEFENCE));
        assertTrue(colonySettlementType.hasAbility(Ability.CONSUME_ALL_OR_NOTHING));
        
        EuropeanNationType buildingNationType = (EuropeanNationType)specification.nationTypes.getById("model.nationType.building");
        assertEquals(3, buildingNationType.getStartedUnits(true).size());
	}

    private void verifySpecificationUnitTypes(Specification specification, Game game) {
    	UnitType unitType = specification.unitTypes.getById("model.unit.flyingDutchman");
    	assertEquals(1, unitType.requiredAbilitiesAmount());
    	
    	UnitType caravel = specification.unitTypes.getById("model.unit.caravel");
    	assertThat(caravel.getPrice()).isEqualTo(1000);
    	assertThat(caravel.hasAbility(Ability.NAVAL_UNIT)).isTrue();
    	assertThat(caravel.applyModifier(Modifier.TRADE_VOLUME_PENALTY, 100)).isEqualTo(25);
    	
    	UnitType freeColonist = specification.unitTypes.getById(UnitType.FREE_COLONIST);
    	assertThat(freeColonist.unitConsumption.getById(GoodsType.FOOD).getQuantity()).isEqualTo(2);
    	assertThat(freeColonist.unitConsumption.getById(GoodsType.BELLS).getQuantity()).isEqualTo(1);
    	
    	UnitType artillery = specification.unitTypes.getById("model.unit.artillery");
    	Player player = game.players.getById("player:1");
    	UnitType damagedArtillery = artillery.upgradeByChangeType(ChangeType.DEMOTION, player);
    	assertThat(damagedArtillery.getId()).isEqualTo("model.unit.damagedArtillery");
	}

	private void verifySpecificationUnitRoles(Specification specification, Game game) {
        UnitRole dragoonUnitRole = specification.unitRoles.getById("model.role.dragoon");
        
        assertEquals(5, dragoonUnitRole.abilitiesAmount());
        assertEquals(3, dragoonUnitRole.modifiersAmount());
        
        assertEquals(3, dragoonUnitRole.requiredAbilitiesAmount());
        assertEquals(2, dragoonUnitRole.requiredGoods.size());
        assertEquals(50, dragoonUnitRole.requiredGoods.getById("model.goods.muskets").amount);
        assertEquals(50, dragoonUnitRole.requiredGoods.getById("model.goods.horses").amount);
        
        MapIdEntities<UnitRoleChange> roleChanges = specification.unitRoles.getById("model.role.mountedBrave")
    		.roleChanges;
        assertThat(roleChanges.entities()).hasSize(3);
        
        assertThat(roleChanges).containsId("model.role.default:model.role.scout");
        assertThat(roleChanges).containsId("model.role.default:model.role.dragoon");
        assertThat(roleChanges).containsId("model.role.default:model.role.cavalry");
        
        verifyCaptureEquipment(specification, game);
    }
	
	private void verifyCaptureEquipment(Specification spec, Game game) {
		Unit brave = new Unit("1", 
			spec.unitTypes.getById("model.unit.brave"), 
			spec.unitRoles.getById("model.role.default"),
			game.players.getById("player:40")
		);
		Unit dragon = new Unit("2", 
			spec.unitTypes.getById("model.unit.freeColonist"), 
			spec.unitRoles.getById("model.role.dragoon"),
			game.players.getById("player:1")
		);
		
		assertThat(brave.canCaptureEquipment(dragon)).isTrue();
		assertThat(brave.capturedEquipment(dragon).getId()).isEqualTo("model.role.mountedBrave");
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
        assertNotNull(building.getUnits().getById("unit:6765"));
        assertNotNull(building.getUnits().getById("unit:6439"));
    }
	
	
}
