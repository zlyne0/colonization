package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitType;
import promitech.colonization.savegame.Savegame1600BaseClass;
import net.sf.freecol.common.model.specification.GoodsType;

import org.junit.jupiter.api.Test;

import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.ObjectsListScoreAssert;

import static org.assertj.core.api.Assertions.assertThat;

class WorkerReqScoreTest extends Savegame1600BaseClass {

	@Test
	void canGenerateRequiredColonistsFortNassau() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(fortNassau);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(fortNassau, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();
//		for (ObjectScore<UnitType> unitTypeObjectScore : colonyScore) {
//			System.out.println(unitTypeObjectScore);
//		}

		// then
		ObjectsListScoreAssert.assertThat(colonyScore)
				.hasSumScore(56)
				.hasScore(0, 0, unitType(UnitType.EXPERT_FISHERMAN))
				.hasScore(1, 36, unitType(UnitType.MASTER_FUR_TRADER))
				.hasScore(2, 20, unitType(UnitType.EXPERT_ORE_MINER))
		;
		assertThat(new ColonySnapshot(fortNassau)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsNieuwAmsterdam() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(nieuwAmsterdam);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(nieuwAmsterdam, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();

		// then
//		for (ObjectScore<UnitType> unitTypeObjectScore : colonyScore) {
//			System.out.println(unitTypeObjectScore);
//		}

		ObjectsListScoreAssert.assertThat(colonyScore)
			.hasSumScore(46)
			.hasScore(0, 30, unitType(UnitType.MASTER_WEAVER))
			.hasScore(1, 16, unitType(UnitType.EXPERT_SILVER_MINER))
		;
		assertThat(new ColonySnapshot(nieuwAmsterdam)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsFortOrange() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(fortOranje);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(fortOranje, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();

		// then
		ObjectsListScoreAssert.assertThat(colonyScore)
			.hasSumScore(45)
			.hasScore(0, 30, unitType(UnitType.MASTER_TOBACCONIST))
			.hasScore(1, 15, unitType(UnitType.EXPERT_FUR_TRAPPER))
		;
		assertThat(new ColonySnapshot(fortOranje)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsFortOrange2() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(fortOranje);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(fortOranje, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();
//		for (ObjectScore<UnitType> unitTypeObjectScore : colonyScore) {
//			System.out.println(unitTypeObjectScore);
//		}

		// then
		ObjectsListScoreAssert.assertThat(colonyScore)
			.hasSumScore(45)
			.hasScore(0, 30, unitType(UnitType.MASTER_TOBACCONIST))
			.hasScore(1, 15, unitType(UnitType.EXPERT_FUR_TRAPPER))
		;
		assertThat(new ColonySnapshot(fortOranje)).isEqualTo(snapshotBefore);
	}


	@Test
	void canGenerateRequiredColonistsFortMaurits() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(fortMaurits);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(fortMaurits, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();
//		for (ObjectScore<UnitType> unitTypeObjectScore : colonyScore) {
//			System.out.println(unitTypeObjectScore);
//		}

		// then
		ObjectsListScoreAssert.assertThat(colonyScore)
			.hasSumScore(106)
			.hasScore(0, 40, unitType(UnitType.MASTER_TOBACCO_PLANTER))
			.hasScore(1, 36, unitType(UnitType.MASTER_FUR_TRADER))
			.hasScore(2, 30, unitType(UnitType.MASTER_TOBACCONIST))
		;
		assertThat(new ColonySnapshot(fortMaurits)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsFortMaurits2() throws Exception {
		// given
		ColonySnapshot snapshotBefore = new ColonySnapshot(fortMaurits);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(fortMaurits, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();
//		for (ObjectScore<UnitType> unitTypeObjectScore : colonyScore) {
//			System.out.println(unitTypeObjectScore);
//		}

		// then
		ObjectsListScoreAssert.assertThat(colonyScore)
				.hasSumScore(106)
				.hasScore(0, 40, unitType(UnitType.MASTER_TOBACCO_PLANTER))
				.hasScore(1, 36, unitType(UnitType.MASTER_FUR_TRADER))
				.hasScore(2, 30, unitType(UnitType.MASTER_TOBACCONIST))
		;
		assertThat(new ColonySnapshot(fortMaurits)).isEqualTo(snapshotBefore);
	}


	@Test
	public void canScoreCreateNewColonyReq() throws Exception {
		// given
		Tile tile1 = game.map.getTile(27, 74);
		Tile tile2 = game.map.getTile(26, 71);
		
		CreateColonyReqScore sut = new CreateColonyReqScore(
			game.map, 
			dutch,
			new MapIdEntities<GoodsType>(Specification.instance.goodsTypeToScoreByPrice)
		);
		
		// when
		ObjectsListScore<TileUnitType> tileScore1 = new ObjectsListScore<>(2);
		sut.score(tileScore1, tile1);
		ObjectsListScore<TileUnitType> tileScore2 = new ObjectsListScore<>(2);
		sut.score(tileScore2, tile2);

		// then
		ObjectsListScoreAssert.assertThat(tileScore1)
			.hasScore2(24, new TileUnitType(tile1, unitType(UnitType.MASTER_FUR_TRADER)))
			.hasScore2(24, new TileUnitType(tile1, unitType(UnitType.FREE_COLONIST)))
			.hasScore2(15, new TileUnitType(tile1, unitType(UnitType.FREE_COLONIST)))
			.hasScore2(24, new TileUnitType(tile1, unitType(UnitType.EXPERT_FUR_TRAPPER)))
		;
		ObjectsListScoreAssert.assertThat(tileScore2)
			.hasScore2(30, new TileUnitType(tile2, unitType(UnitType.MASTER_TOBACCONIST)))
			.hasScore2(30, new TileUnitType(tile2, unitType(UnitType.FREE_COLONIST)))
			.hasScore2(21, new TileUnitType(tile2, unitType(UnitType.FREE_COLONIST)))
			.hasScore2(30, new TileUnitType(tile2, unitType(UnitType.EXPERT_FUR_TRAPPER)))
		;
	}
}
