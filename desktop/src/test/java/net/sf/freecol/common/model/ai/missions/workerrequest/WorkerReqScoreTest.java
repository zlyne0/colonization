package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.specification.GoodsType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import promitech.colonization.ai.Units;
import promitech.colonization.ai.score.ScoreableObjectsList;
import promitech.colonization.ai.score.ScoreableObjectsListAssert;
import promitech.colonization.savegame.Savegame1600BaseClass;

import static net.sf.freecol.common.model.ai.missions.workerrequest.WorkerRequestScoreValueComparator.eq;
import static org.assertj.core.api.Assertions.assertThat;

class WorkerReqScoreTest extends Savegame1600BaseClass {

	EntryPointTurnRange entryPointTurnRange;

	@BeforeEach
	public void beforeEach() {
		entryPointTurnRange = new EntryPointTurnRange(game.map, new PathFinder(), dutch, Units.findCarrier(dutch));
	}

	@Test
	void canGenerateRequiredColonistsFortNassau() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(fortNassau);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(fortNassau, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ScoreableObjectsList<WorkerRequestScoreValue> colonyScore = new ScoreableObjectsList<>(20);
		sut.simulate(colonyScore);

		ScorePolicy.WorkerProductionValue workerProductionValueScorePolicy = new ScorePolicy.WorkerProductionValue(entryPointTurnRange);
		workerProductionValueScorePolicy.calculateScore(colonyScore);
		colonyScore.prettyPrint();

		// then
		ScoreableObjectsListAssert.assertThat(colonyScore)
			.hasSumScore(61)
			.hasScore(0, 35, eq(UnitType.EXPERT_FISHERMAN))
			.hasScore(1, 26, eq(UnitType.FREE_COLONIST))
		;
		ScoreableObjectsListAssert.assertThat(((MultipleWorkerRequestScoreValue)colonyScore.get(0)).getWorkersScores())
			.hasSumScore(84)
			.hasScore(0, 0, eq(UnitType.EXPERT_FISHERMAN))
			.hasScore(1, 48, eq(UnitType.MASTER_FUR_TRADER))
			.hasScore(2, 36, eq(UnitType.EXPERT_ORE_MINER))
		;
		ScoreableObjectsListAssert.assertThat(((MultipleWorkerRequestScoreValue)colonyScore.get(1)).getWorkersScores())
			.hasSumScore(60)
			.hasScore(0, 0, eq(UnitType.FREE_COLONIST))
			.hasScore(1, 36, eq(UnitType.FREE_COLONIST))
			.hasScore(2, 24, eq(UnitType.FREE_COLONIST))
		;

		assertThat(new ColonySnapshot(fortNassau)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsNieuwAmsterdam() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(nieuwAmsterdam);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(nieuwAmsterdam, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ScoreableObjectsList<WorkerRequestScoreValue> colonyScore = new ScoreableObjectsList<>(20);
		sut.simulate(colonyScore);

		ScorePolicy.WorkerProductionValue workerProductionValueScorePolicy = new ScorePolicy.WorkerProductionValue(entryPointTurnRange);
		workerProductionValueScorePolicy.calculateScore(colonyScore);
		colonyScore.prettyPrint();

		// then
		ScoreableObjectsListAssert.assertThat(colonyScore)
			.hasSumScore(60)
			.hasScore(0, 30, eq(UnitType.MASTER_WEAVER))
			.hasScore(1, 30, eq(UnitType.FREE_COLONIST))
		;
		ScoreableObjectsListAssert.assertThat(((MultipleWorkerRequestScoreValue)colonyScore.get(0)).getWorkersScores())
			.hasSumScore(54)
			.hasScore(0, 30, eq(UnitType.MASTER_WEAVER))
			.hasScore(1, 24, eq(UnitType.EXPERT_FUR_TRAPPER))
		;
		ScoreableObjectsListAssert.assertThat(((MultipleWorkerRequestScoreValue)colonyScore.get(1)).getWorkersScores())
			.hasSumScore(46)
			.hasScore(0, 30, eq(UnitType.FREE_COLONIST))
			.hasScore(1, 16, eq(UnitType.FREE_COLONIST))
		;
		assertThat(new ColonySnapshot(nieuwAmsterdam)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsFortOrange() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(fortOranje);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(fortOranje, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ScoreableObjectsList<WorkerRequestScoreValue> colonyScore = new ScoreableObjectsList<>(20);
		sut.simulate(colonyScore);

		ScorePolicy.WorkerProductionValue workerProductionValueScorePolicy = new ScorePolicy.WorkerProductionValue(entryPointTurnRange);
		workerProductionValueScorePolicy.calculateScore(colonyScore);
		colonyScore.prettyPrint();

		// then
		ScoreableObjectsListAssert.assertThat(colonyScore)
			.hasSumScore(70)
			.hasScore(0, 40, eq(UnitType.MASTER_TOBACCONIST))
			.hasScore(1, 30, eq(UnitType.FREE_COLONIST))
		;
		assertThat(new ColonySnapshot(fortOranje)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsFortMaurits() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(fortMaurits);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(fortMaurits, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ScoreableObjectsList<WorkerRequestScoreValue> colonyScore = new ScoreableObjectsList<>(20);
		sut.simulate(colonyScore);

		ScorePolicy.WorkerProductionValue workerProductionValueScorePolicy = new ScorePolicy.WorkerProductionValue(entryPointTurnRange);
		workerProductionValueScorePolicy.calculateScore(colonyScore);
		colonyScore.prettyPrint();

		// then
		ScoreableObjectsListAssert.assertThat(colonyScore)
			.hasSumScore(102)
			.hasScore(0, 67, eq(UnitType.MASTER_FUR_TRADER))
			.hasScore(1, 35, eq(UnitType.FREE_COLONIST))
		;
		assertThat(new ColonySnapshot(fortMaurits)).isEqualTo(snapshotBefore);
	}

	@Test
	void canScoreCreateNewColonyReq() throws Exception {
		// given
		Tile tile1 = game.map.getTile(27, 74);
		Tile tile2 = game.map.getTile(26, 71);
		
		CreateColonyReqScore sut = new CreateColonyReqScore(
			game.map, 
			dutch,
			new MapIdEntities<GoodsType>(Specification.instance.goodsTypeToScoreByPrice)
		);
		
		// when
		ScoreableObjectsList<WorkerRequestScoreValue> tileScore1 = new ScoreableObjectsList<>(2);
		sut.score(tileScore1, tile1);
		ScoreableObjectsList<WorkerRequestScoreValue> tileScore2 = new ScoreableObjectsList<>(2);
		sut.score(tileScore2, tile2);

		// then
		tileScore1.prettyPrint();
		ScoreableObjectsListAssert.assertThat(tileScore1)
			.hasSize(4)
			.hasScore(0, 24, eq(tile1, UnitType.FREE_COLONIST))
			.hasScore(1,24, eq(tile1, UnitType.MASTER_FUR_TRADER))
			.hasScore(2,15, eq(tile1, UnitType.FREE_COLONIST))
			.hasScore(3,24, eq(tile1, UnitType.EXPERT_FUR_TRAPPER))
		;

		tileScore2.prettyPrint();
		ScoreableObjectsListAssert.assertThat(tileScore2)
			.hasSize(4)
			.hasScore(0,30, eq(tile2, UnitType.FREE_COLONIST))
			.hasScore(1,30, eq(tile2, UnitType.MASTER_TOBACCONIST))
			.hasScore(2,21, eq(tile2, UnitType.FREE_COLONIST))
			.hasScore(3,30, eq(tile2, UnitType.EXPERT_FUR_TRAPPER))
		;
	}
}
