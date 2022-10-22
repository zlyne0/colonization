package net.sf.freecol.common.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.utils.ObjectIntMap;

import net.sf.freecol.common.model.Colony.NoBuildReason;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;
import promitech.colonization.savegame.SaveGameParser;

public class ColonyTest {

	Game game;
	Player dutch;
	BuildingType stockadeType;
	
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new Lwjgl3Files();
    }
	
    @BeforeEach
    public void setup() throws Exception {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	dutch = game.players.getById("player:1");
		stockadeType = Specification.instance.buildingTypes.getById("model.building.stockade");
    }
    
	@Test
	public void shouldDisallowBuildUnit() throws Exception {
		// given
        Colony colony = dutch.settlements.getById("colony:6528").asColony();
        
        UnitType freeColonist = Specification.instance.unitTypes.getById("model.unit.freeColonist");
        
		// when
        NoBuildReason noBuildReason = colony.getNoBuildReason(freeColonist);
		
		// then
        assertEquals(NoBuildReason.MISSING_BUILD_ABILITY, noBuildReason);
	}

	@Test
	public void canCalculateTurnsToCompleteBuilding() throws Exception {
		// given
		Colony colony = dutch.settlements.getById("colony:6528").asColony();
		colony.getGoodsContainer().increaseGoodsQuantity("model.goods.hammers", 100);
		colony.getGoodsContainer().decreaseToZero("model.goods.tools");

		BuildingType runDistilleryType = Specification.instance.buildingTypes.getById("model.building.rumDistillery");
		
		// when
		ObjectIntMap<String> requiredTurnsForGoods = new ObjectIntMap<String>(2);
		int turnsToComplete = colony.getTurnsToComplete(runDistilleryType, requiredTurnsForGoods);

		// then
		assertEquals(Colony.NEVER_COMPLETE_BUILD, turnsToComplete);
	}

}
