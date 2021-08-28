package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.GoodsType;

import org.junit.jupiter.api.Test;

import promitech.colonization.ai.score.ScoreableObjectsList;
import promitech.colonization.ai.score.ScoreableObjectsListAssert;
import promitech.colonization.savegame.Savegame1600BaseClass;

import static net.sf.freecol.common.model.ai.missions.workerrequest.WorkerRequestScoreValueComparator.eq;
import static net.sf.freecol.common.model.ai.missions.workerrequest.WorkerRequestScoreValueComparator.unitTypeEq;
import static org.assertj.core.api.Assertions.assertThat;

class WorkerReqScoreTest extends Savegame1600BaseClass {

	@Test
	void canGenerateRequiredColonistsFortNassau() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(fortNassau);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(fortNassau, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ScoreableObjectsList<WorkerRequestScoreValue> colonyScore = sut.simulate();
//		for (ObjectsListScore.ObjectScore<WorkerRequestScoreValue> unitTypeObjectScore : colonyScore) {
//			System.out.println(unitTypeObjectScore);
//		}

		// then
		ScoreableObjectsListAssert.assertThat(colonyScore)
			.hasSumScore(84)
			.hasScore(0, 0, unitTypeEq(UnitType.EXPERT_FISHERMAN))
			.hasScore(1, 48, unitTypeEq(UnitType.MASTER_FUR_TRADER))
			.hasScore(2, 36, unitTypeEq(UnitType.EXPERT_ORE_MINER))
		;
		assertThat(new ColonySnapshot(fortNassau)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsNieuwAmsterdam() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(nieuwAmsterdam);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(nieuwAmsterdam, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ScoreableObjectsList<WorkerRequestScoreValue> colonyScore = sut.simulate();

		// then
//		for (ObjectsListScore.ObjectScore<WorkerRequestScoreValue> unitTypeObjectScore : colonyScore) {
//			System.out.println(unitTypeObjectScore);
//		}

		ScoreableObjectsListAssert.assertThat(colonyScore)
			.hasSumScore(54)
			.hasScore(0, 30, unitTypeEq(UnitType.MASTER_WEAVER))
			.hasScore(1, 24, unitTypeEq(UnitType.EXPERT_FUR_TRAPPER))
		;
		assertThat(new ColonySnapshot(nieuwAmsterdam)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsFortOrange() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(fortOranje);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(fortOranje, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ScoreableObjectsList<WorkerRequestScoreValue> colonyScore = sut.simulate();
//		for (ObjectScore<UnitType> unitTypeObjectScore : colonyScore) {
//			System.out.println(unitTypeObjectScore);
//		}

		// then
		ScoreableObjectsListAssert.assertThat(colonyScore)
			.hasSumScore(64)
			.hasScore(0, 40, unitTypeEq(UnitType.MASTER_TOBACCONIST))
			.hasScore(1, 24, unitTypeEq(UnitType.EXPERT_FUR_TRAPPER))
		;
		assertThat(new ColonySnapshot(fortOranje)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsFortMaurits() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(fortMaurits);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(fortMaurits, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ScoreableObjectsList<WorkerRequestScoreValue> colonyScore = sut.simulate();
//		for (ObjectsListScore.ObjectScore<WorkerRequestScoreValue> unitTypeObjectScore : colonyScore) {
//			System.out.println(unitTypeObjectScore);
//		}

		// then
		ScoreableObjectsListAssert.assertThat(colonyScore)
			.hasSumScore(196)
			.hasScore(0, 72, unitTypeEq(UnitType.MASTER_FUR_TRADER))
			.hasScore(1, 64, unitTypeEq(UnitType.MASTER_TOBACCO_PLANTER))
			.hasScore(2, 60, unitTypeEq(UnitType.MASTER_TOBACCONIST))
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
