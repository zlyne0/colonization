package net.sf.freecol.common.model.ai.missions.workerrequest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.ai.ObjectScoreAssert;
import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.ai.ObjectsListScore.ObjectScore;
import promitech.colonization.ai.ObjectsListScoreAssert;
import promitech.colonization.savegame.SaveGameParser;

class WorkerReqScoreTest {

	Game game;
	Player dutch;
	
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }
    
    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	dutch = game.players.getById("player:1");
    }
	
	@Test
	void canGenerateRequiredColonistsFortNassau() throws Exception {
		// given
    	Colony fortNassau = game.map.getTile(20, 79).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(fortNassau);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(fortNassau, Specification.instance.goodsTypeToScoreByPrice);
		
		System.out.println("production");
		System.out.println(fortNassau.productionSummary().toString());
		
		
		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();
		
		// then
		ObjectsListScoreAssert.assertThat(colonyScore)
			.hasSumScore(52)
			.hasScore(0, 0, unitType(UnitType.EXPERT_FISHERMAN))
			.hasScore(1, 36, unitType(UnitType.MASTER_FUR_TRADER))
			.hasScore(2, 16, unitType(UnitType.EXPERT_ORE_MINER))
		;
		assertThat(new ColonySnapshot(fortNassau)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsFortNassau2() throws Exception {
		// given
		Colony fortNassau = game.map.getTile(20, 79).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(fortNassau);
		ColonyWorkerReqScore2 sut = new ColonyWorkerReqScore2(fortNassau, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();
//		for (ObjectScore<UnitType> unitTypeObjectScore : colonyScore) {
//			System.out.println(unitTypeObjectScore);
//		}

		// then
		ObjectsListScoreAssert.assertThat(colonyScore)
				.hasSumScore(36)
				.hasScore(0, 0, unitType(UnitType.EXPERT_FISHERMAN))
				.hasScore(1, 36, unitType(UnitType.MASTER_FUR_TRADER))
		;
		assertThat(new ColonySnapshot(fortNassau)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsNieuwAmsterdam() throws Exception {
		// given
    	Colony colony = game.map.getTile(24, 78).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(colony);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(colony, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();

		// then
		ObjectsListScoreAssert.assertThat(colonyScore)
			.hasSumScore(42)
			.hasScore(0, 30, unitType(UnitType.MASTER_WEAVER))
			.hasScore(1, 0, unitType(UnitType.EXPERT_FISHERMAN))
			.hasScore(2, 12, unitType(UnitType.EXPERT_FUR_TRAPPER))
		;
		assertThat(new ColonySnapshot(colony)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsNieuwAmsterdam2() throws Exception {
		// given
    	Colony colony = game.map.getTile(24, 78).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(colony);
		ColonyWorkerReqScore2 sut = new ColonyWorkerReqScore2(colony, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();

		// then
//		for (ObjectScore<UnitType> unitTypeObjectScore : colonyScore) {
//			System.out.println(unitTypeObjectScore);
//		}

		ObjectsListScoreAssert.assertThat(colonyScore)
			.hasSumScore(61)
			.hasScore(0, 30, unitType(UnitType.MASTER_WEAVER))
			.hasScore(1, 16, unitType(UnitType.EXPERT_SILVER_MINER))
			.hasScore(2, 15, unitType(UnitType.EXPERT_FUR_TRAPPER))
		;
		assertThat(new ColonySnapshot(colony)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsFortOrange() throws Exception {
		// given
    	Colony colony = game.map.getTile(25, 75).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(colony);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(colony, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();

		// then
		ObjectsListScoreAssert.assertThat(colonyScore)
			.hasSumScore(45)
			.hasScore(0, 30, unitType(UnitType.MASTER_TOBACCONIST))
			.hasScore(1, 15, unitType(UnitType.EXPERT_FUR_TRAPPER))
		;
		assertThat(new ColonySnapshot(colony)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsFortOrange2() throws Exception {
		// given
		Colony colony = game.map.getTile(25, 75).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(colony);
		ColonyWorkerReqScore2 sut = new ColonyWorkerReqScore2(colony, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();
//		for (ObjectScore<UnitType> unitTypeObjectScore : colonyScore) {
//			System.out.println(unitTypeObjectScore);
//		}

		// then
		ObjectsListScoreAssert.assertThat(colonyScore)
			.hasSumScore(81)
			.hasScore(0, 30, unitType(UnitType.MASTER_TOBACCONIST))
			.hasScore(1, 15, unitType(UnitType.EXPERT_FUR_TRAPPER))
			.hasScore(2, 36, unitType(UnitType.MASTER_FUR_TRADER))
		;
		assertThat(new ColonySnapshot(colony)).isEqualTo(snapshotBefore);
	}


	@Test
	void canGenerateRequiredColonistsFortMaurits() throws Exception {
		// given
    	Colony colony = game.map.getTile(21, 72).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(colony);
		ColonyWorkerReqScore sut = new ColonyWorkerReqScore(colony, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();

		// then
		ObjectsListScoreAssert.assertThat(colonyScore)
			.hasSumScore(106)
			.hasScore(0, 40, unitType(UnitType.MASTER_TOBACCO_PLANTER))
			.hasScore(1, 36, unitType(UnitType.MASTER_FUR_TRADER))
			.hasScore(2, 30, unitType(UnitType.MASTER_TOBACCONIST))
		;
		assertThat(new ColonySnapshot(colony)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsFortMaurits2() throws Exception {
		// given
		Colony colony = game.map.getTile(21, 72).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(colony);
		ColonyWorkerReqScore2 sut = new ColonyWorkerReqScore2(colony, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();
		for (ObjectScore<UnitType> unitTypeObjectScore : colonyScore) {
			System.out.println(unitTypeObjectScore);
		}

		// then
		ObjectsListScoreAssert.assertThat(colonyScore)
				.hasSumScore(116)
				.hasScore(0, 40, unitType(UnitType.MASTER_TOBACCO_PLANTER))
				.hasScore(1, 36, unitType(UnitType.MASTER_FUR_TRADER))
				.hasScore(2, 40, unitType(UnitType.MASTER_TOBACCO_PLANTER))
		;
		assertThat(new ColonySnapshot(colony)).isEqualTo(snapshotBefore);
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
	
	UnitType unitType(String unitTypeId) {
		return Specification.instance.unitTypes.getById(unitTypeId);
	}
}
