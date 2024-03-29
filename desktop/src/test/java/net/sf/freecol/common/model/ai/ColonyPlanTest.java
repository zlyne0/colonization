package net.sf.freecol.common.model.ai;

import com.badlogic.gdx.utils.ObjectIntMap;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyAssert;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.colonyproduction.ColonyPlan;
import net.sf.freecol.common.model.colonyproduction.MaxGoodsProductionLocation;
import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.player.Market;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;

import org.junit.jupiter.api.Test;

import java.util.List;

import promitech.colonization.Direction;
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
		colonyPlan.execute(ColonyPlan.Plan.Food).allocateWorkers();

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
		colonyPlan.execute(ColonyPlan.Plan.Bell).allocateWorkers();

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
		colonyPlan.execute(ColonyPlan.Plan.Bell).allocateWorkers();

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
		colonyPlan.execute(ColonyPlan.Plan.Bell, ColonyPlan.Plan.Food).allocateWorkers();

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
		colonyPlan.execute(ColonyPlan.Plan.Building).allocateWorkers();

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
		colonyPlan.execute(ColonyPlan.Plan.Tools).allocateWorkers();

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
		colonyPlan.execute(ColonyPlan.Plan.Tools).allocateWorkers();

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
		colonyPlan.execute(ColonyPlan.Plan.Tools, ColonyPlan.Plan.Bell).allocateWorkers();

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
		colonyPlan.execute(ColonyPlan.Plan.Muskets).allocateWorkers();

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

    	UnitType fisherman = unitType(UnitType.EXPERT_FISHERMAN);
    	
		// when
    	List<MaxGoodsProductionLocation> productions = nieuwAmsterdam.productionSimulation().determinePotentialMaxGoodsProduction(fisherman, false);
		
		// then
    	for (MaxGoodsProductionLocation gpl : productions) {
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
		colonyPlan.execute(ColonyPlan.Plan.Food).allocateWorkers();

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
		colonyPlan.execute(ColonyPlan.Plan.Food).allocateWorkers();

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

		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam)
			.withConsumeWarehouseResources(false)
		;

		// when
		colonyPlan.execute(ColonyPlan.Plan.MostValuable).allocateWorkers();

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(6)
			.hasWorkerInBuildingType(BuildingType.FUR_TRADER_HOUSE, UnitType.MASTER_FUR_TRADER)
			.hasWorkerInBuildingType(BuildingType.FUR_TRADER_HOUSE, 3)
			.hasWorkerInLocation(tileFrom(nieuwAmsterdam, Direction.NE).id, UnitType.EXPERT_FUR_TRAPPER)
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
		colonyPlan.execute(ColonyPlan.Plan.MostValuable).allocateWorkers();

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

	@Test
	void shouldChangeBuildingProductionWhenFullWarehouse() {
		// given
		nieuwAmsterdam.getGoodsContainer().increaseGoodsQuantity(GoodsType.COAST, nieuwAmsterdam.warehouseCapacity());

		// when
		new ColonyPlan(nieuwAmsterdam)
			.withConsumeWarehouseResources(true)
			.execute(ColonyPlan.Plan.MostValuable)
			.allocateWorkers();
		printColonyWorkers(nieuwAmsterdam);

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(6)
			.hasNoWorkerInBuildingType("model.building.furTraderHouse")
		;
	}

	@Test
	void shouldChangeTileProductionWhenFullWarehouse() {
		// given
		fortMaurits.getGoodsContainer().decreaseAllToZero();
		fortMaurits.getGoodsContainer().increaseGoodsQuantity(GoodsType.TOBACCO, fortMaurits.warehouseCapacity());

		ColonyPlan colonyPlan = new ColonyPlan(fortMaurits);
		colonyPlan.withConsumeWarehouseResources(true);

		// when
		colonyPlan.execute(ColonyPlan.Plan.MostValuable).allocateWorkers();
		//printColonyWorkers(fortMaurits);

		// then
		ColonyAssert.assertThat(fortMaurits)
			.hasSize(2)
			.produce(GoodsType.TOBACCO, 0)
		;
	}

	@Test
	void shouldResetWorkersAllocationOnExecutePlan() {
		// given
		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(true);

		// when
		colonyPlan.execute2(ColonyPlan.Plan.MostValuable, ColonyPlan.Plan.Food);
		ProductionSummary productionSummary1 = colonyPlan.productionConsumption().cloneGoods();

		colonyPlan.execute2(ColonyPlan.Plan.Building, ColonyPlan.Plan.Food);
		ProductionSummary buildingproductionSummary1 = colonyPlan.productionConsumption().cloneGoods();

		colonyPlan.execute2(ColonyPlan.Plan.MostValuable, ColonyPlan.Plan.Food);
		ProductionSummary productionSummary2 = colonyPlan.productionConsumption().cloneGoods();

		colonyPlan.execute2(ColonyPlan.Plan.Building, ColonyPlan.Plan.Food);
		ProductionSummary buildingproductionSummary2 = colonyPlan.productionConsumption().cloneGoods();

		// then
		assertThat(buildingproductionSummary1).isEqualTo(buildingproductionSummary2);
		assertThat(productionSummary1).isEqualTo(productionSummary2);
	}

	@Test
	void shouldIncreaseProductionValueWhenWarehouseIsFullAndUpgradeWarehouse() {
		// given
		givenFullWarehouse(nieuwAmsterdam);

		BuildingType warehouseExpansion = Specification.instance.buildingTypes.getById(BuildingType.WAREHOUSE_EXPANSION);

		// when
		ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(true);
		colonyPlan.execute2(ColonyPlan.Plan.MostValuable, ColonyPlan.Plan.Food);
		ProductionSummary productionSummary1 = colonyPlan.productionConsumption().cloneGoods();

		ColonyPlan colonyPlan2 = new ColonyPlan(nieuwAmsterdam);
		colonyPlan2.withConsumeWarehouseResources(true);
		colonyPlan2.addBuilding(warehouseExpansion);
		colonyPlan2.execute2(ColonyPlan.Plan.MostValuable, ColonyPlan.Plan.Food);
		ProductionSummary productionSummary2 = colonyPlan2.productionConsumption().cloneGoods();

		// then
		Market market = dutch.market();
		int valueGold1 = market.getSalePrice(productionSummary1);
		int valueGold2 = market.getSalePrice(productionSummary2);
		assertThat(valueGold1).isEqualTo(112);
		assertThat(valueGold2).isEqualTo(154);
		assertThat(valueGold2 > valueGold1).isTrue();
		assertThat(valueGold2 - valueGold1).isEqualTo(42);
	}

	void givenFullWarehouse(Colony colony) {
		for (ObjectIntMap.Entry<String> entry : colony.getGoodsContainer().cloneGoods().entries()) {
			colony.getGoodsContainer().decreaseToZero(entry.key);
			colony.getGoodsContainer().increaseGoodsQuantity(entry.key, colony.warehouseCapacity());
		}
	}

	private void printColonyWorkers(Colony colony) {
    	System.out.println("XXXXX printColonyWorkers size " + colony.settlementWorkers().size());
        for (Unit unit : colony.settlementWorkers()) {
            System.out.println("XX colony worker " + unit.toStringTypeLocation());
        }
        System.out.println("XX productionConsumption " + colony.productionSummary());
        System.out.println("XX warehause " + colony.getGoodsContainer().cloneGoods());
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
