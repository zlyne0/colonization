package net.sf.freecol.common.model.ai;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.ai.ObjectsListScoreAssert;
import promitech.colonization.ai.ObjectsListScore;
import promitech.colonization.savegame.SaveGameParser;

class ColonyWorkerReqTest {

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
    	Colony colony = game.map.getTile(20, 79).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(colony);
		ColonyWorkerReq sut = new ColonyWorkerReq(colony, Specification.instance.goodsTypeToScoreByPrice);
		
		// when
		ObjectsListScore<UnitType> colonyScore = sut.simulate();

		// then
		
		ObjectsListScoreAssert.assertThat(colonyScore)
			.hasSumScore(36)
			.hasScore(0, 0, unitType(UnitType.EXPERT_FISHERMAN))
			.hasScore(1, 36, unitType(UnitType.MASTER_FUR_TRADER))
		;
		assertThat(new ColonySnapshot(colony)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsNieuwAmsterdam() throws Exception {
		// given
    	Colony colony = game.map.getTile(24, 78).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(colony);
		ColonyWorkerReq sut = new ColonyWorkerReq(colony, Specification.instance.goodsTypeToScoreByPrice);

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
	void canGenerateRequiredColonistsFortOrange() throws Exception {
		// given
    	Colony colony = game.map.getTile(25, 75).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(colony);
		ColonyWorkerReq sut = new ColonyWorkerReq(colony, Specification.instance.goodsTypeToScoreByPrice);

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
	void canGenerateRequiredColonistsFortMaurits() throws Exception {
		// given
    	Colony colony = game.map.getTile(21, 72).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(colony);
		ColonyWorkerReq sut = new ColonyWorkerReq(colony, Specification.instance.goodsTypeToScoreByPrice);

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
	
	UnitType unitType(String unitTypeId) {
		return Specification.instance.unitTypes.getById(unitTypeId);
	}
}
