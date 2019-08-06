package net.sf.freecol.common.model.ai;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyAssert;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ProductionInfoAssert;
import net.sf.freecol.common.model.ProductionSummaryAssert;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitLocationAssert;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.SaveGameParser;

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
    	colonyPlan.execute(ColonyPlan.Plan.Food);

		// then
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
    	colonyPlan.execute(ColonyPlan.Plan.Bell);

		// then
    	ColonyAssert.assertThat(nieuwAmsterdam).hasSize(3);
    	
        UnitLocationAssert.assertThat(nieuwAmsterdam.findBuildingByType(BuildingType.TOWN_HALL))
            .hasSize(3)
            .hasUnit("unit:7076")
            .hasUnitType("model.unit.elderStatesman");
    	
    	ProductionSummaryAssert.assertThat(nieuwAmsterdam.productionSummary())
    	    .hasNoLessThenZero(GoodsType.FOOD);
	}

    @Test
	public void canHandleBellPlanAndAddFoodWorker() throws Exception {
		// given
    	nieuwAmsterdam.tile.removeTileImprovement(TileImprovementType.PLOWED_IMPROVEMENT_TYPE_ID);
    	nieuwAmsterdam.tile.removeTileImprovement(TileImprovementType.ROAD_MODEL_IMPROVEMENT_TYPE_ID);
    	nieuwAmsterdam.tile.changeTileType(Specification.instance.tileTypes.getById("model.tile.broadleafForest"));
    	
    	ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);
    	
		// when
    	colonyPlan.execute(ColonyPlan.Plan.Bell);

		// then
        for (Unit unit : nieuwAmsterdam.settlementWorkers()) {
            System.out.println("colony worker " + unit.toStringTypeLocation());
        }
        System.out.println("productionConsumption " + nieuwAmsterdam.productionSummary());
    	
        ColonyAssert.assertThat(nieuwAmsterdam).hasSize(4);
    	
    	UnitLocationAssert.assertThat(nieuwAmsterdam.findBuildingByType(BuildingType.TOWN_HALL))
        	.hasSize(3)
        	.hasUnit("unit:7076")
        	.hasUnitType("model.unit.elderStatesman");
    	
        ProductionSummaryAssert.assertThat(nieuwAmsterdam.productionSummary())
            .hasNoLessThenZero(GoodsType.FOOD);
	}
    
    @Test
    void canExecuteBellFoodPlan() throws Exception {
        // given
        ColonyPlan colonyPlan = new ColonyPlan(nieuwAmsterdam);

        // when
        colonyPlan.execute(ColonyPlan.Plan.Bell, ColonyPlan.Plan.Food);

        // then
        for (Unit unit : nieuwAmsterdam.settlementWorkers()) {
            System.out.println("colony worker " + unit.toStringTypeLocation());
        }
        System.out.println("productionConsumption " + nieuwAmsterdam.productionSummary());

        ColonyAssert.assertThat(nieuwAmsterdam).hasSize(6);
        ProductionSummaryAssert.assertThat(nieuwAmsterdam.productionSummary())
            .has(GoodsType.FOOD, 11);
        
        UnitLocationAssert.assertThat(nieuwAmsterdam.findBuildingByType(BuildingType.TOWN_HALL))
            .hasSize(3)
            .hasUnit("unit:7076")
            .hasUnitType("model.unit.elderStatesman");

        UnitLocationAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3472"))
            .hasUnit("unit:7096")
            .hasUnitType("model.unit.expertFisherman");
        UnitLocationAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3431"))
            .hasUnit("unit:6940");
        UnitLocationAssert.assertThat(nieuwAmsterdam.colonyTiles.getById("tile:3432"))
            .hasUnit("unit:6939");
    }
}
