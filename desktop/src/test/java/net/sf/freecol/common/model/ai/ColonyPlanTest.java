package net.sf.freecol.common.model.ai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ColonyTile;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ProductionInfoAssert;
import net.sf.freecol.common.model.player.Player;
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

}
