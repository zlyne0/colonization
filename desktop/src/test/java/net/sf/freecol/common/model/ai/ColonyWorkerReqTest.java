package net.sf.freecol.common.model.ai;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

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
import promitech.colonization.ai.ObjectsListScore.ObjectScore;
import promitech.colonization.savegame.SaveGameParser;

class ColonyWorkerReqTest {

	Game game;
	Player dutch;
	Colony nieuwAmsterdam;
	Colony fortNassau;
	Colony fortOrange;
	Colony fortMaurits;
	
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }
    
    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	dutch = game.players.getById("player:1");
    	nieuwAmsterdam = game.map.getTile(24, 78).getSettlement().asColony();
    	fortNassau = game.map.getTile(20, 79).getSettlement().asColony();
    	fortOrange = game.map.getTile(25, 75).getSettlement().asColony();
    	fortMaurits = game.map.getTile(21, 72).getSettlement().asColony();
    }
	
	@Test
	void canGenerateRequiredColonistsFortNassau() throws Exception {
		// given
		ColonyWorkersSnapshot snapshotBefore = new ColonyWorkersSnapshot(fortNassau);
		ColonyWorkerReq sut = new ColonyWorkerReq(fortNassau, Specification.instance.goodsTypeToScoreByPrice);
		
		
		// when
		ObjectScore<List<UnitType>> colonyScore = sut.simulate();

		System.out.println(colonyScore);
		
		// then
		assertThat(colonyScore.getScore()).isEqualTo(36);
		assertThat(colonyScore.getObj())
			.containsExactly(
				Specification.instance.unitTypes.getById(UnitType.EXPERT_FISHERMAN),
				Specification.instance.unitTypes.getById(UnitType.MASTER_FUR_TRADER)
			);
		assertThat(new ColonyWorkersSnapshot(fortNassau)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsNieuwAmsterdam() throws Exception {
		// given
		ColonyWorkersSnapshot snapshotBefore = new ColonyWorkersSnapshot(nieuwAmsterdam);
		ColonyWorkerReq sut = new ColonyWorkerReq(nieuwAmsterdam, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectScore<List<UnitType>> colonyScore = sut.simulate();

		System.out.println(colonyScore);
		
		// then
		assertThat(colonyScore.getScore()).isEqualTo(56);
		assertThat(colonyScore.getObj())
			.containsExactly(
				Specification.instance.unitTypes.getById(UnitType.MASTER_FUR_TRADER),
				Specification.instance.unitTypes.getById(UnitType.EXPERT_FISHERMAN),
				Specification.instance.unitTypes.getById(UnitType.MASTER_TOBACCONIST)
			);
		assertThat(new ColonyWorkersSnapshot(nieuwAmsterdam)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsFortOrange() throws Exception {
		// given
		ColonyWorkersSnapshot snapshotBefore = new ColonyWorkersSnapshot(fortOrange);
		ColonyWorkerReq sut = new ColonyWorkerReq(fortOrange, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectScore<List<UnitType>> colonyScore = sut.simulate();

		System.out.println(colonyScore);
		
		// then
		assertThat(colonyScore.getScore()).isEqualTo(60);
		assertThat(colonyScore.getObj())
			.containsExactly(
				Specification.instance.unitTypes.getById(UnitType.MASTER_TOBACCONIST),
				Specification.instance.unitTypes.getById(UnitType.MASTER_TOBACCONIST)
			);
		assertThat(new ColonyWorkersSnapshot(fortOrange)).isEqualTo(snapshotBefore);
	}
	
	@Test
	void canGenerateRequiredColonistsFortMaurits() throws Exception {
		// given
		ColonyWorkersSnapshot snapshotBefore = new ColonyWorkersSnapshot(fortMaurits);
		ColonyWorkerReq sut = new ColonyWorkerReq(fortMaurits, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectScore<List<UnitType>> colonyScore = sut.simulate();

		System.out.println(colonyScore);
		
		// then
		assertThat(colonyScore.getScore()).isEqualTo(112);
		assertThat(colonyScore.getObj())
			.containsExactly(
				Specification.instance.unitTypes.getById(UnitType.MASTER_TOBACCO_PLANTER),
				Specification.instance.unitTypes.getById(UnitType.MASTER_FUR_TRADER),
				Specification.instance.unitTypes.getById(UnitType.MASTER_FUR_TRADER)
			);
		assertThat(new ColonyWorkersSnapshot(fortMaurits)).isEqualTo(snapshotBefore);
	}
	
	
}
