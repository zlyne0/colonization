package net.sf.freecol.common.model.ai;

import net.sf.freecol.common.model.ColonyAssert;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.colonyproduction.ColonyPlan;
import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;

import org.junit.jupiter.api.Test;

import java.util.List;

import promitech.colonization.savegame.Savegame1600BaseClass;
import promitech.map.isometric.NeighbourIterableTile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class ColonyPlanTest extends Savegame1600BaseClass {

	@Test
	void canGenerateFoodPlanForColony() {
		// given
		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);

		// when
		colonyPlan.execute(new ColonyPlan.Plan.Food());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(6)
			.hasProductionOnTile("tile:3472", UnitType.EXPERT_FISHERMAN, GoodsType.FISH, 10)
			.hasProductionOnTile("tile:3432", GoodsType.FISH, 7)

			.hasProductionOnTile("tile:3431", GoodsType.GRAIN, 6)
			.hasProductionOnTile("tile:3393", GoodsType.FISH, 4)
			.hasProductionOnTile("tile:3391", GoodsType.GRAIN, 3)
			.hasProductionOnTile("tile:3352", GoodsType.GRAIN, 3)
			.produce(GoodsType.FOOD, 19)
			.produce(GoodsType.HORSES, 8)
		;
	}

	@Test
	void canHandleBellPlan() {
		// given
		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);

		// when
		colonyPlan.execute(new ColonyPlan.Plan.Bell());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(3)
			.hasWorkerInBuildingType(BuildingType.TOWN_HALL, 3)
			.hasWorkerInBuildingType(BuildingType.TOWN_HALL, UnitType.ELDER_STATESMAN)
			.produce(GoodsType.FOOD, 0)
		;
	}

	@Test
	void canHandleBellPlanAndAddFoodWorker() {
		// given
		addForestOnColonyCenterTile();

		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);

		// when
		colonyPlan.execute(new ColonyPlan.Plan.Bell());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(4)
			.hasWorkerInBuildingType(BuildingType.TOWN_HALL, 3)
			.hasWorkerInBuildingType(BuildingType.TOWN_HALL, UnitType.ELDER_STATESMAN)
			.produce(GoodsType.FOOD, 3)
		;
	}

	@Test
	void canExecuteBellFoodPlan() {
		// given
		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);

		// when
		colonyPlan.execute(new ColonyPlan.Plan.Bell(), new ColonyPlan.Plan.Food());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(6)
			.hasWorkerInBuildingType(BuildingType.TOWN_HALL, 3)
			.hasWorkerInBuildingType(BuildingType.TOWN_HALL, UnitType.ELDER_STATESMAN)
			.hasWorkerInLocation("tile:3472", UnitType.EXPERT_FISHERMAN)
			.hasWorkerInLocation("tile:3431")
			.hasWorkerInLocation("tile:3432")
			.produce(GoodsType.BELLS, 14)
			.produce(GoodsType.FOOD, 11)
		;
	}

	@Test
	void canExecuteBuildPlan() {
		// given
		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(false);

		// when
		colonyPlan.execute(new ColonyPlan.Plan.Building());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(6)
			.hasWorkerInLocation("tile:3352", "model.unit.expertLumberJack")
			.hasWorkerInBuildingType("model.building.lumberMill", 2)
			.hasWorkerInBuildingType("model.building.lumberMill", "model.unit.masterCarpenter")
			.produce("model.goods.lumber", 6)
			.produce("model.goods.hammers", 18)
		;
	}

	@Test
	void canExecuteToolsPlanWithoutWarehouseResources() {
		// given
		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(false);

		// when
		colonyPlan.execute(new ColonyPlan.Plan.Tools());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(6)
			.hasWorkerInLocation("tile:3391")
			.hasWorkerInLocation("tile:3351")
			.hasWorkerInLocation("tile:3431")
			.hasWorkerInLocation("tile:3472", "model.unit.expertFisherman")
			.hasWorkerInBuildingType("model.building.blacksmithHouse", 2)
			.produce("model.goods.ore", -1)
			.produce("model.goods.tools", 6)
		;
	}

	@Test
	void canExecuteToolsPlanWithWarehouseResources() {
		// given
		addForestOnColonyCenterTile();

		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(true);

		// when
		colonyPlan.execute(new ColonyPlan.Plan.Tools());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasWorkerInBuildingType("model.building.blacksmithHouse", 3)
			.hasWorkerInLocation("tile:3472", "model.unit.expertFisherman")
			.produce("model.goods.ore", -9)
			.produce("model.goods.tools", 9)
			.hasSize(4)
		;
	}

	@Test
	void canExecutePlanForToolsAndBell() {
		// given
		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(true);

		// when
		colonyPlan.execute(new ColonyPlan.Plan.Tools(), new ColonyPlan.Plan.Bell());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(6)
			.hasWorkerInBuildingType("model.building.blacksmithHouse", 3)
			.hasWorkerInLocation("tile:3472", "model.unit.expertFisherman")
			.hasWorkerInBuildingType(BuildingType.TOWN_HALL, 2)
			.hasWorkerInBuildingType(BuildingType.TOWN_HALL, "model.unit.elderStatesman")
		;
	}

	@Test
	void canAssignWorkersToProduceMuskets() {
		// given
		nieuwAmsterdam.addBuilding(Specification.instance.buildingTypes.getById("model.building.armory"));

		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(false);

		// when
		colonyPlan.execute(new ColonyPlan.Plan.Muskets());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(6)
			.hasWorkerInLocation("tile:3391")
			.hasWorkerInLocation("tile:3351")
			.hasWorkerInLocation("tile:3472", "model.unit.expertFisherman")
			.hasWorkerInBuildingType("model.building.armory")
			.hasWorkerInBuildingType("model.building.blacksmithHouse")
			.produce("model.goods.ore", 2)
			.produce("model.goods.tools", 0)
			.produce("model.goods.muskets", 3)
		;
	}


    @Test
	void noProductionLocationForLockedTiles() {
		// given
    	lockAllTilesInColony();
    	
    	UnitType fisherman = Specification.instance.unitTypes.getById(UnitType.EXPERT_FISHERMAN);
    	
		// when
    	List<GoodMaxProductionLocation> productions = nieuwAmsterdam.productionSimulation().determinePotentialMaxGoodsProduction(fisherman, false);
		
		// then
    	for (GoodMaxProductionLocation gpl : productions) {
    		//System.out.println("" + gpl.getProductionLocation() + " " + gpl.getGoodsType() + " " + gpl.getProduction());
    		if (gpl.getColonyTile() != null) {
    			fail("should no production on tile because all tile should be locked");
    		}
		}
	}
    
	@Test
	void endExecutePlanWhenCanNotFindWorkLocation() {
		// given
		lockAllTilesInColony();

		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);

		// when
		colonyPlan.execute(new ColonyPlan.Plan.Food());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(0);
	}

	@Test
	void canAssignWorkersWhenTilesLockedAndIgnoreIndianOwner() {
		// given
		lockAllTilesInColony();

		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
		colonyPlan.withIgnoreIndianOwner();

		// when
		colonyPlan.execute(new ColonyPlan.Plan.Food());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(5);
	}

	@Test
	void canCreateProductionChainForMuskets() {
		// given

		// when
		List<GoodsType> productionChain = goodsType(GoodsType.MUSKETS).productionChain();

		// then
		assertThat(productionChain)
			.extracting(GoodsType::getId)
			.containsExactly(GoodsType.ORE, GoodsType.TOOLS, GoodsType.MUSKETS);
	}

	@Test
	void canCreateProductionChainForBells() {
		// given

		// when
		List<GoodsType> productionChain = goodsType(GoodsType.BELLS).productionChain();

		// then
		assertThat(productionChain)
			.extracting(GoodsType::getId)
			.containsExactly(GoodsType.BELLS);
	}

	@Test
	void canAssignWorkersToMostValueableProductionNotUsingWarehouseResources2() {
		// given
		addForestOnColonyCenterTile();
		nieuwAmsterdam.resetLiberty();

		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(false);

		// when
		colonyPlan.execute(new ColonyPlan.Plan.MostValuable());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(6)
			.hasWorkerInBuildingType("model.building.furTraderHouse", "model.unit.masterFurTrader")
			.hasWorkerInBuildingType("model.building.furTraderHouse", 3)
			.hasWorkerInLocation("tile:3352", "model.unit.expertFurTrapper")
			.produce("model.goods.coats", 12)
			.produce("model.goods.furs", 2)
			.produce("model.goods.food", 0)
			.produce("model.goods.horses", 0)
		;
	}

	@Test
	void canAssignWorkersToMostValueableProductionUsingWarehouseResources2() {
		// given
		addForestOnColonyCenterTile();
		nieuwAmsterdam.resetLiberty();

		nieuwAmsterdam.getGoodsContainer().decreaseToZero(GoodsType.FOOD);
		nieuwAmsterdam.getGoodsContainer().decreaseToZero(GoodsType.HORSES);
		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(true);

		// when
		colonyPlan.execute(new ColonyPlan.Plan.MostValuable());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(6)
			.hasWorkerInBuildingType("model.building.furTraderHouse", "model.unit.masterFurTrader")
			.hasWorkerInBuildingType("model.building.furTraderHouse", 3)
			.hasWorkerInBuildingType("model.building.tobacconistHouse", 2)
			.hasWorkerInLocation("tile:3472", "model.unit.expertFisherman")
			.produce("model.goods.coats", 12)
			.produce("model.goods.furs", -10)
			.produce("model.goods.tobacco", -6)
			.produce("model.goods.cigars", 6)
			.produce("model.goods.food", 0)
			.produce("model.goods.horses", 0)
		;
	}


    private void printColonyWorkers() {
    	System.out.println("XXXXX printColonyWorkers size " + nieuwAmsterdam.settlementWorkers().size());
        for (Unit unit : nieuwAmsterdam.settlementWorkers()) {
            System.out.println("XX colony worker " + unit.toStringTypeLocation());
        }
        System.out.println("XX productionConsumption " + nieuwAmsterdam.productionSummary());
        System.out.println("XX warehause " + nieuwAmsterdam.getGoodsContainer().cloneGoods());
    }
    
    private void lockAllTilesInColony() {
    	nieuwAmsterdam.getOwner().foundingFathers.removeId(FoundingFather.PETER_MINUIT);
    	nieuwAmsterdam.removeBuilding("model.building.docks");
    	nieuwAmsterdam.updateModelOnWorkerAllocationOrGoodsTransfer();
    	nieuwAmsterdam.updateColonyFeatures();
    	
    	Player inca = game.players.getById("player:154");
    	for (NeighbourIterableTile<Tile> neighbourTile : game.map.neighbourLandTiles(nieuwAmsterdam.tile)) {
    		neighbourTile.tile.changeOwner(inca);
    		neighbourTile.tile.changeOwner(inca, inca.settlements.getById("indianSettlement:6339"));
		}
    }
    
    void addForestOnColonyCenterTile() {
    	nieuwAmsterdam.tile.removeTileImprovement(TileImprovementType.PLOWED_IMPROVEMENT_TYPE_ID);
    	nieuwAmsterdam.tile.removeTileImprovement(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID);
    	nieuwAmsterdam.tile.changeTileType(Specification.instance.tileTypes.getById("model.tile.broadleafForest"));
    	nieuwAmsterdam.updateProductionToMaxPossible(nieuwAmsterdam.tile);
    }
    
}
