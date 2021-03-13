package net.sf.freecol.common.model.ai.missions.workerrequest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.ai.MapTileDebugInfo;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.ObjectsListScoreAssert;
import promitech.colonization.ai.Units;
import promitech.colonization.savegame.SaveGameParser;
import promitech.colonization.savegame.Savegame1600BaseClass;

class ColonyWorkerRequestPlanerTest extends Savegame1600BaseClass {

	MapTileDebugInfo mapDebugInfo = new MapTileDebugInfo() {
		@Override
		public void str(int x, int y, String str) {
		}
	};
	
	MapIdEntities<GoodsType> goodsTypeToScore;

    @BeforeEach
    public void setup2() throws Exception {
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
		ObjectsListScoreAssert.assertThat(scores)
			.hasScore2(0, 106, new TileUnitType(fortMaurits.tile, unitType(UnitType.MASTER_TOBACCO_PLANTER)))
			.hasScore2(1, 60, new TileUnitType(game.map.getSafeTile(13, 76), unitType(UnitType.MASTER_TOBACCO_PLANTER)))
			.hasScore2(2, 56, new TileUnitType(fortNassau.tile, unitType(UnitType.EXPERT_FISHERMAN)))
			.hasScore2(3, 46, new TileUnitType(nieuwAmsterdam.tile, unitType(UnitType.MASTER_WEAVER)))
			.hasScore2(4, 45, new TileUnitType(fortOranje.tile, unitType(UnitType.MASTER_TOBACCONIST)))
		;
	}
}
