package net.sf.freecol.common.model.ai.missions.workerrequest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.ai.MapTileDebugInfo;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.Units;
import promitech.colonization.savegame.SaveGameParser;

class ColonyWorkerRequestPlanerTest {

	MapTileDebugInfo mapDebugInfo = new MapTileDebugInfo() {
		@Override
		public void str(int x, int y, String str) {
		}
	};
	
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
	public void canScoreTiles() throws Exception {
		// given
		PathFinder pathFinder = new PathFinder();
		ColonyWorkerRequestPlaner sut = new ColonyWorkerRequestPlaner(game, pathFinder);
		
		Unit transporter = Units.findCarrier(dutch);
		
		// when
		ObjectsListScore<TileUnitType> scores = sut.score(dutch, transporter);
		sut.debug(mapDebugInfo);
		

		// then
		assertThat(scores.size()).isEqualTo(10);
		assertThat(scores.get(0).getScore()).isEqualTo(106);
		assertThat(scores.get(0).getObj().unitType.getId()).isEqualTo(UnitType.MASTER_TOBACCO_PLANTER);
		assertThat(scores.get(0).getObj().tile.getId()).isEqualTo("tile:3149");
		assertThat(scores.get(1).getScore()).isEqualTo(45);
		assertThat(scores.get(1).getObj().unitType.getId()).isEqualTo(UnitType.MASTER_TOBACCONIST);
		assertThat(scores.get(1).getObj().tile.getId()).isEqualTo("tile:3273");
	}
}
