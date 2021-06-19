package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.ai.MapTileDebugInfo;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.specification.GoodsType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.ObjectsListScoreAssert;
import promitech.colonization.ai.Units;
import promitech.colonization.savegame.Savegame1600BaseClass;

import static net.sf.freecol.common.model.ai.missions.workerrequest.WorkerRequestScoreValueComparator.eq;

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
	void canScoreTiles() throws Exception {
		// given
		PathFinder pathFinder = new PathFinder();
		ColonyWorkerRequestPlaner sut = new ColonyWorkerRequestPlaner(game.map, pathFinder);
		
		Unit transporter = Units.findCarrier(dutch);
		
		// when
		ObjectsListScore<WorkerRequestScoreValue> scores = sut.score(dutch, transporter);
		//scores.prettyPrint();
		sut.debug(mapDebugInfo);

		// then
		ObjectsListScoreAssert.assertThat(scores)
			.hasScore(0, 72, eq(fortMaurits.tile, UnitType.MASTER_FUR_TRADER))
			.hasScore(1, 60, eq(game.map.getSafeTile(13, 76), UnitType.MASTER_TOBACCO_PLANTER))
			.hasScore(2, 48, eq(fortNassau.tile, UnitType.EXPERT_FISHERMAN))
			.hasScore(3, 40, eq(fortOranje.tile, UnitType.MASTER_TOBACCONIST))
			.hasScore(4, 40, eq(game.map.getSafeTile(15, 80), UnitType.EXPERT_ORE_MINER))
			.hasScore(5, 37, eq(game.map.getSafeTile(19, 81), UnitType.MASTER_TOBACCO_PLANTER))
		;
	}
}
