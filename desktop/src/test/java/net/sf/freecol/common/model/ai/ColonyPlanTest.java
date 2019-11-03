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
import net.sf.freecol.common.model.ProductionInfoAssert;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.player.FoundingFather;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.SaveGameParser;
import promitech.map.isometric.NeighbourIterableTile;

class ColonyPlanTest {

	Game game;
	Player dutch;
	Colony nieuwAmsterdam;
	
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }

    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	dutch = game.players.getById("player:1");
    	nieuwAmsterdam = game.map.getTile(24, 78).getSettlement().asColony();
    }

    @Test
	public void canGenerateFoodPlanForColony() throws Exception {
		// given
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	
		// when
    	colonyPlan.execute2(ColonyPlan.Plan.Food);

		// then
    	ColonyAssert.assertThat(nieuwAmsterdam).hasSize(6);
    	
    	ProductionInfoAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3472").productionInfo)
    		.hasOutput(GoodsType.FISH, 2);
    	ProductionInfoAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3432").productionInfo)
    		.hasOutput(GoodsType.FISH, 2);
    	ProductionInfoAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3393").productionInfo)
    		.hasOutput(GoodsType.FISH, 2);
    	ProductionInfoAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3431").productionInfo)
			.hasOutput(GoodsType.GRAIN, 5);
    	ProductionInfoAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3352").productionInfo)
			.hasOutput(GoodsType.GRAIN, 3);
    	ProductionInfoAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3391").productionInfo)
			.hasOutput(GoodsType.GRAIN, 3);
    	ProductionInfoAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3392").productionInfo)
    		.hasOutput(GoodsType.GRAIN, 5, true);
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
            .hasWorkerInBuildingType(BuildingType.TOWN_HALL, "model.unit.elderStatesman")
            .produce(GoodsType.FOOD, 0)
	    ;
	}

    @Test
	public void canHandleBellPlanAndAddFoodWorker() throws Exception {
		// given
    	forestOnColonyCenterTile();
    	
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	
		// when
    	colonyPlan.execute2(ColonyPlan.Plan.Bell);

    	// then
        ColonyAssert.assertThat(nieuwAmsterdam)
            .hasSize(4)
            .hasWorkerInBuildingType(BuildingType.TOWN_HALL, 3)
            .hasWorkerInBuildingType(BuildingType.TOWN_HALL, "model.unit.elderStatesman")
            .produce(GoodsType.FOOD, 5)
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
            .hasWorkerInBuildingType(BuildingType.TOWN_HALL, "model.unit.elderStatesman")
            .hasWorkerInLocation("tile:3472", "model.unit.expertFisherman")
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
	void canExecuteToolsPlanWithWarehouseResources() throws Exception {
		// given
    	forestOnColonyCenterTile();
    	
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
	    	.hasWorkerInBuildingType("model.building.blacksmithHouse", 2)
            .produce("model.goods.ore", -2)
            .produce("model.goods.tools", 3)
            .produce("model.goods.muskets", 3)
    	;
	}

    @Test
	public void noProductionLocationForLockedTiles() throws Exception {
		// given
    	lockAllTilesInColony();
    	
    	Unit fisherman = UnitFactory.create("model.unit.expertFisherman", dutch, nieuwAmsterdam.tile);
    	
		// when
    	List<GoodMaxProductionLocation> productions = nieuwAmsterdam.determinePotentialMaxGoodsProduction(fisherman);
		
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
	public void testName() throws Exception {
		// given
    	forestOnColonyCenterTile();
    	
    	nieuwAmsterdam.getGoodsContainer().decreaseAllToZero();
    	nieuwAmsterdam.resetLiberty();
    	
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	colonyPlan.withConsumeWarehouseResources(false);
    	
		// when
		colonyPlan.mostValueable();

		// then
		printColonyWorkers();
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
    
    void forestOnColonyCenterTile() {
    	nieuwAmsterdam.tile.removeTileImprovement(TileImprovementType.PLOWED_IMPROVEMENT_TYPE_ID);
    	nieuwAmsterdam.tile.removeTileImprovement(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID);
    	nieuwAmsterdam.tile.changeTileType(Specification.instance.tileTypes.getById("model.tile.broadleafForest"));
    	nieuwAmsterdam.initMaxPossibleProductionOnTile(nieuwAmsterdam.tile);
    }
    
}
