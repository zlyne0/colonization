package net.sf.freecol.common.model.ai;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyAssert;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.GoodMaxProductionLocation;
import net.sf.freecol.common.model.ProductionAssert;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.ai.ColonyPlan.Plan;
import net.sf.freecol.common.model.colonyproduction.ColonyPlan2;
import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.savegame.Savegame1600BaseClass;
import promitech.map.isometric.NeighbourIterableTile;

class ColonyPlanTest extends Savegame1600BaseClass {

	@Test
	public void canGenerateFoodPlanForColony() throws Exception {
		// given
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	
		// when
    	colonyPlan.execute2(ColonyPlan.Plan.Food);

		// then
    	ColonyAssert.assertThat(nieuwAmsterdam).hasSize(6);
    	
    	ProductionAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3472").production)
    		.hasOutput(GoodsType.FISH, 2);
    	ProductionAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3432").production)
    		.hasOutput(GoodsType.FISH, 2);
    	ProductionAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3393").production)
    		.hasOutput(GoodsType.FISH, 2);
    	ProductionAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3431").production)
			.hasOutput(GoodsType.GRAIN, 5);
    	ProductionAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3352").production)
			.hasOutput(GoodsType.GRAIN, 3);
    	ProductionAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3391").production)
			.hasOutput(GoodsType.GRAIN, 3);
    	ProductionAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3392").production)
    		.hasOutput(GoodsType.GRAIN, 5, true);
	}

	@Test
	void canGenerateFoodPlanForColony2() {
		// given
		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);

		// when
		colonyPlan.execute(new ColonyPlan2.Plan.Food());

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
	public void canHandleBellPlan() throws Exception {
		// given
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	
		// when
    	colonyPlan.execute2(ColonyPlan.Plan.Bell);

		// then
    	ColonyAssert.assertThat(nieuwAmsterdam)
    	    .hasSize(3)
            .hasWorkerInBuildingType(BuildingType.TOWN_HALL, 3)
            .hasWorkerInBuildingType(BuildingType.TOWN_HALL, UnitType.ELDER_STATESMAN)
            .produce(GoodsType.FOOD, 0)
	    ;
	}

	@Test
	public void canHandleBellPlan2() throws Exception {
		// given
		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);

		// when
		colonyPlan.execute(new ColonyPlan2.Plan.Bell());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(3)
			.hasWorkerInBuildingType(BuildingType.TOWN_HALL, 3)
			.hasWorkerInBuildingType(BuildingType.TOWN_HALL, UnitType.ELDER_STATESMAN)
			.produce(GoodsType.FOOD, 0)
		;
	}


    @Test
	public void canHandleBellPlanAndAddFoodWorker() throws Exception {
		// given
    	addForestOnColonyCenterTile();
    	
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	
		// when
    	colonyPlan.execute2(ColonyPlan.Plan.Bell);

    	// then
        ColonyAssert.assertThat(nieuwAmsterdam)
            .hasSize(4)
            .hasWorkerInBuildingType(BuildingType.TOWN_HALL, 3)
            .hasWorkerInBuildingType(BuildingType.TOWN_HALL, UnitType.ELDER_STATESMAN)
            .produce(GoodsType.FOOD, 3)
        ;
	}

	@Test
	public void canHandleBellPlanAndAddFoodWorker2() throws Exception {
		// given
		addForestOnColonyCenterTile();

		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);

		// when
		colonyPlan.execute(new ColonyPlan2.Plan.Bell());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(4)
			.hasWorkerInBuildingType(BuildingType.TOWN_HALL, 3)
			.hasWorkerInBuildingType(BuildingType.TOWN_HALL, UnitType.ELDER_STATESMAN)
			.produce(GoodsType.FOOD, 3)
		;
	}

    @Test
    void canExecuteBellFoodPlan() throws Exception {
        // given
        ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);

        // when
        colonyPlan.execute2(ColonyPlan.Plan.Bell, ColonyPlan.Plan.Food);

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
	void canExecuteBellFoodPlan2() throws Exception {
		// given
		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);

		// when
		colonyPlan.execute(new ColonyPlan2.Plan.Bell(), new ColonyPlan2.Plan.Food());

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
    void canExecuteBuildPlan() throws Exception {
        // given
        ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
        colonyPlan.withConsumeWarehouseResources(false);
        
        // when
        colonyPlan.execute2(ColonyPlan.Plan.Building);

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
	void canExecuteBuildPlan2() throws Exception {
		// given
		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(false);

		// when
		colonyPlan.execute(new ColonyPlan2.Plan.Building());

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
	void canExecuteToolsPlanWithoutWarehouseResources() throws Exception {
		// given
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	colonyPlan.withConsumeWarehouseResources(false);
    	
		// when
    	colonyPlan.execute2(ColonyPlan.Plan.Tools);

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
	void canExecuteToolsPlanWithoutWarehouseResources2() throws Exception {
		// given
		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(false);

		// when
		colonyPlan.execute(new ColonyPlan2.Plan.Tools());

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
	void canExecuteToolsPlanWithWarehouseResources() throws Exception {
		// given
    	addForestOnColonyCenterTile();
    	
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	colonyPlan.withConsumeWarehouseResources(true);
    	
		// when
    	colonyPlan.execute2(ColonyPlan.Plan.Tools);

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
	void canExecuteToolsPlanWithWarehouseResources2() throws Exception {
		// given
		addForestOnColonyCenterTile();

		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(true);

		// when
		colonyPlan.execute(new ColonyPlan2.Plan.Tools());

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
	public void canExecutePlanForToolsAndBell() throws Exception {
		// given
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	colonyPlan.withConsumeWarehouseResources(true);
    	
		// when
    	colonyPlan.execute2(ColonyPlan.Plan.Tools, ColonyPlan.Plan.Bell);

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
	public void canExecutePlanForToolsAndBell2() throws Exception {
		// given
		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(true);

		// when
		colonyPlan.execute(new ColonyPlan2.Plan.Tools(), new ColonyPlan2.Plan.Bell());

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
	public void canAssignWorkersToProduceMuskets() throws Exception {
		// given
    	nieuwAmsterdam.addBuilding(Specification.instance.buildingTypes.getById("model.building.armory"));
    	
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	colonyPlan.withConsumeWarehouseResources(false);
    	
		// when
    	colonyPlan.execute2(ColonyPlan.Plan.Muskets);

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
	public void canAssignWorkersToProduceMuskets2() throws Exception {
		// given
		nieuwAmsterdam.addBuilding(Specification.instance.buildingTypes.getById("model.building.armory"));

		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(false);

		// when
		colonyPlan.execute(new ColonyPlan2.Plan.Muskets());

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
	public void noProductionLocationForLockedTiles() throws Exception {
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
	public void endExecutePlanWhenCanNotFindWorkLocation() throws Exception {
		// given
    	lockAllTilesInColony();
    	
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	
		// when
    	colonyPlan.execute2(ColonyPlan.Plan.Food);		

		// then
    	ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(0);
	}

	@Test
	public void endExecutePlanWhenCanNotFindWorkLocation2() throws Exception {
		// given
		lockAllTilesInColony();

		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);

		// when
		colonyPlan.execute(new ColonyPlan2.Plan.Food());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(0);
	}

    @Test
	public void canAssignWorkersWhenTilesLockedAndIgnoreIndianOwner() throws Exception {
		// given
    	lockAllTilesInColony();
    	
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	colonyPlan.withIgnoreIndianOwner();
    	
		// when
    	colonyPlan.execute2(ColonyPlan.Plan.Food);		

		// then
    	ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(5);
	}

	@Test
	public void canAssignWorkersWhenTilesLockedAndIgnoreIndianOwner2() throws Exception {
		// given
		lockAllTilesInColony();

		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);
		colonyPlan.withIgnoreIndianOwner();

		// when
		colonyPlan.execute(new ColonyPlan2.Plan.Food());

		// then
		ColonyAssert.assertThat(nieuwAmsterdam)
			.hasSize(5);
	}

	@Test
	void canCreateProductionChainForMuskets() {
		// given
		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);

		// when
		List<GoodsType> productionChain = colonyPlan.createPlanProductionChain(new ColonyPlan2.Plan.Muskets());

		// then
		assertThat(productionChain)
			.extracting(GoodsType::getId)
			.containsExactly(GoodsType.ORE, GoodsType.TOOLS, GoodsType.MUSKETS);
	}

	@Test
	void canCreateProductionChainForBells() {
		// given
		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);

		// when
		List<GoodsType> productionChain = colonyPlan.createPlanProductionChain(new ColonyPlan2.Plan.Bell());

		// then
		assertThat(productionChain)
			.extracting(GoodsType::getId)
			.containsExactly(GoodsType.BELLS);
	}

	@Test
	public void canAssignWorkersToMostValueableProductionNotUsingWarehouseResources() throws Exception {
		// given
    	addForestOnColonyCenterTile();
    	nieuwAmsterdam.resetLiberty();
    	
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	colonyPlan.withConsumeWarehouseResources(false);
    	
		// when
		colonyPlan.execute2(Plan.MostValuable);

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
	public void canAssignWorkersToMostValueableProductionNotUsingWarehouseResources2() throws Exception {
		// given
		addForestOnColonyCenterTile();
		nieuwAmsterdam.resetLiberty();

		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(false);

		// when
		colonyPlan.execute(new ColonyPlan2.Plan.MostValuable());

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
	public void canAssignWorkersToMostValueableProductionUsingWarehouseResources() throws Exception {
		// given
    	addForestOnColonyCenterTile();
    	nieuwAmsterdam.resetLiberty();
    	
    	nieuwAmsterdam.getGoodsContainer().decreaseToZero(GoodsType.FOOD);
    	nieuwAmsterdam.getGoodsContainer().decreaseToZero(GoodsType.HORSES);
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	colonyPlan.withConsumeWarehouseResources(true);
    	
		// when
    	colonyPlan.execute2(Plan.MostValuable);

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
	public void canAssignWorkersToMostValueableProductionUsingWarehouseResources2() throws Exception {
		// given
		addForestOnColonyCenterTile();
		nieuwAmsterdam.resetLiberty();

		nieuwAmsterdam.getGoodsContainer().decreaseToZero(GoodsType.FOOD);
		nieuwAmsterdam.getGoodsContainer().decreaseToZero(GoodsType.HORSES);
		ColonyPlan2 colonyPlan = new ColonyPlan2(nieuwAmsterdam);
		colonyPlan.withConsumeWarehouseResources(true);

		// when
		colonyPlan.execute(new ColonyPlan2.Plan.MostValuable());

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
