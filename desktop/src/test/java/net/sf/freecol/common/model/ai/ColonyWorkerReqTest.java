package net.sf.freecol.common.model.ai;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Assertions;
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
		ObjectScore<List<UnitType>> colonyScore = sut.simulate();

		// then
		assertThat(colonyScore.getScore()).isEqualTo(36);
		assertThat(colonyScore.getObj())
			.containsExactly(
				Specification.instance.unitTypes.getById(UnitType.EXPERT_FISHERMAN),
				Specification.instance.unitTypes.getById(UnitType.MASTER_FUR_TRADER)
			);
		assertThat(new ColonySnapshot(colony)).isEqualTo(snapshotBefore);
	}

	@Test
	void canGenerateRequiredColonistsNieuwAmsterdam() throws Exception {
		// given
    	Colony colony = game.map.getTile(24, 78).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(colony);
		ColonyWorkerReq sut = new ColonyWorkerReq(colony, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectScore<List<UnitType>> colonyScore = sut.simulate();

		// then
		Assertions.assertAll(
			() -> assertThat(colonyScore.getScore()).isEqualTo(42),
			() -> assertThat(colonyScore.getObj())
				.containsExactly(
					Specification.instance.unitTypes.getById(UnitType.MASTER_WEAVER),
					Specification.instance.unitTypes.getById(UnitType.EXPERT_FISHERMAN),
					Specification.instance.unitTypes.getById(UnitType.EXPERT_FUR_TRAPPER)
				),
			() -> assertThat(new ColonySnapshot(colony)).isEqualTo(snapshotBefore)
		);
	}

	@Test
	void canGenerateRequiredColonistsFortOrange() throws Exception {
		// given
    	Colony colony = game.map.getTile(25, 75).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(colony);
		ColonyWorkerReq sut = new ColonyWorkerReq(colony, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectScore<List<UnitType>> colonyScore = sut.simulate();

		// then
		assertThat(colonyScore.getScore()).isEqualTo(45);
		assertThat(colonyScore.getObj())
			.containsExactly(
				Specification.instance.unitTypes.getById(UnitType.MASTER_TOBACCONIST),
				Specification.instance.unitTypes.getById(UnitType.EXPERT_FUR_TRAPPER)
			);
		assertThat(new ColonySnapshot(colony)).isEqualTo(snapshotBefore);
	}
	
	@Test
	void canGenerateRequiredColonistsFortMaurits() throws Exception {
		// given
    	Colony colony = game.map.getTile(21, 72).getSettlement().asColony();
		ColonySnapshot snapshotBefore = new ColonySnapshot(colony);
		ColonyWorkerReq sut = new ColonyWorkerReq(colony, Specification.instance.goodsTypeToScoreByPrice);

		// when
		ObjectScore<List<UnitType>> colonyScore = sut.simulate();

		// then
		assertThat(colonyScore.getScore()).isEqualTo(106);
		assertThat(colonyScore.getObj())
			.containsExactly(
				Specification.instance.unitTypes.getById(UnitType.MASTER_TOBACCO_PLANTER),
				Specification.instance.unitTypes.getById(UnitType.MASTER_FUR_TRADER),
				Specification.instance.unitTypes.getById(UnitType.MASTER_TOBACCONIST)
			);
		assertThat(new ColonySnapshot(colony)).isEqualTo(snapshotBefore);
	}
}
