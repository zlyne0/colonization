package net.sf.freecol.common.model.ai.missions.workerrequest;

import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.utils.ObjectIntMap;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Production;
import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.ai.missions.workerrequest.ColonyWorkerRequestPlaner;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;
import promitech.colonization.savegame.SaveGameParser;
import promitech.map.isometric.NeighbourIterableTile;

class ColonyWorkerRequestPlanerTest {

	Game game;
	Player dutch;
	List<GoodsType> goodsTypeToScore;
	
    @BeforeAll
    public static void beforeClass() throws Exception {
        Gdx.files = new LwjglFiles();
    }

    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	dutch = game.players.getById("player:1");
    	
    	goodsTypeToScore = Specification.instance.goodsTypeToScoreByPrice;
    }
	
	
	@Test
	public void testName() throws Exception {
		// given
		ColonyWorkerRequestPlaner sut = new ColonyWorkerRequestPlaner(dutch);
		
		// when
		sut.plan();
		

		// then
	}
}
