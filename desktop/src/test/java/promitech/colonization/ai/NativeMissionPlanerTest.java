package promitech.colonization.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.indian.DemandTributeMission;
import net.sf.freecol.common.model.ai.missions.indian.IndianBringGiftMission;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.MockedRandomizer;
import promitech.colonization.Randomizer;
import promitech.colonization.savegame.SaveGameParser;

class NativeMissionPlanerTest {

	PathFinder pathFinder = new PathFinder();
	NativeMissionPlaner sut;
	
	Game game;
	Player inca;
	
    @BeforeAll
    public static void beforeClass() throws Exception {
        Gdx.files = new Lwjgl3Files();
    }

    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	inca = game.players.getById("player:154");
    	
    	sut = new NativeMissionPlaner(pathFinder);
    }
	
	@Test
	void canCreateGringGiftsMission() throws Exception {
		// given
		PlayerMissionsContainer missionsContainer = new PlayerMissionsContainer(inca);

        Randomizer.changeRandomObject(new MockedRandomizer()
            .withIntsResults(0)
        );
		
		// when
		sut.prepareBringGiftsMission(game.map, inca, missionsContainer);

		// then
		boolean missionExists = sut.isMissionFromSettlementExists(
			"indianSettlement:6339", 
			missionsContainer, 
			IndianBringGiftMission.class
		);
		assertThat(missionExists).isTrue();
	}

	@Test
	void canCreateDemandTributeMission() throws Exception {
		// given
		PlayerMissionsContainer missionsContainer = new PlayerMissionsContainer(inca);

        Randomizer.changeRandomObject(new MockedRandomizer()
            .withIntsResults(0)
        );
		
		// when
		sut.prepareDemandTributeMission(game.map, inca, missionsContainer);

		// then
		boolean missionExists = sut.isMissionFromSettlementExists(
			"indianSettlement:6339", 
			missionsContainer, 
			DemandTributeMission.class
		);
		assertThat(missionExists).isTrue();
	}
}
