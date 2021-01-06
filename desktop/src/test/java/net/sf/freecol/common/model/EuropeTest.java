package net.sf.freecol.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.SaveGameParser;

public class EuropeTest {

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
	public void canNotBuyBecauseNoBudget() throws Exception {
		// given
    	UnitType freeColonist = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
    	int budget = 100;
    	
		// when
		boolean canBuyUnit = dutch.getEurope().canAiBuyUnit(freeColonist, budget);

		// then
		assertThat(canBuyUnit).isFalse();
	}

    @Test
	public void canBuy() throws Exception {
		// given
    	UnitType freeColonist = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
    	int budget = 1000;
    	
		// when
		boolean canBuyUnit = dutch.getEurope().canAiBuyUnit(freeColonist, budget);

		// then
		assertThat(canBuyUnit).isTrue();
	}
    
    @Test
    public void canNotBuyUnitBecauseOnlyNewWorldTrainable() throws Exception {
    	// given
    	UnitType tabaccoPlanter = Specification.instance.unitTypes.getById(UnitType.MASTER_TOBACCO_PLANTER);
    	int budget = 10000;
    	
    	// when
    	boolean canBuyUnit = dutch.getEurope().canAiBuyUnit(tabaccoPlanter, budget);
    	
    	// then
    	assertThat(canBuyUnit).isFalse();
    }
    
    @Test
	public void canRecruitImmigrant() throws Exception {
		// given
    	int gold = dutch.getGold();
    	int immigrantPrice = dutch.getEurope().getRecruitImmigrantPrice();
    	
    	UnitType freeColonist = Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST);
    	
		// when
		Unit unit = dutch.getEurope().buyUnitByAI(freeColonist);

		// then
		UnitAssert.assertThat(unit)
			.isUnitType(freeColonist)
			.isAtLocation(dutch.getEurope());
		assertThat(dutch.getGold()).isEqualTo(gold - immigrantPrice);
	}
    
    @Test
	public void canTrain() throws Exception {
		// given
    	int gold = dutch.getGold();
    	UnitType furTrader = Specification.instance.unitTypes.getById(UnitType.MASTER_FUR_TRADER);
    	
		// when
		Unit unit = dutch.getEurope().buyUnitByAI(furTrader);

		// then
		UnitAssert.assertThat(unit)
			.isUnitType(furTrader)
			.isAtLocation(dutch.getEurope());
		
		assertThat(dutch.getGold()).isEqualTo(gold - furTrader.getPrice());
	}
}
