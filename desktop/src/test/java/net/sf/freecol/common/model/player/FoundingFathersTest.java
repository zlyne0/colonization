package net.sf.freecol.common.model.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import promitech.colonization.savegame.SaveGameParser;

import static org.assertj.core.api.Assertions.assertThat;

class FoundingFathersTest {

    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new Lwjgl3Files();
    }

    Game game;
    Player dutch;
    FoundingFather williamBrewster;
    
    @BeforeEach
    public void setup() throws IOException, ParserConfigurationException, SAXException {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	dutch = game.players.getById("player:1");
    	williamBrewster = Specification.instance.foundingFathers.getById("model.foundingFather.williamBrewster");
    }
	
    @Test
	void onlyColonistsAfterAddedWilliamBrewster() {
		// given
    	List<UnitType> recruitables = dutch.getEurope().getRecruitables();
    	recruitables.clear();
    	recruitables.add(Specification.instance.unitTypes.getById("model.unit.indenturedServant"));
    	recruitables.add(Specification.instance.unitTypes.getById("model.unit.pettyCriminal"));
    	recruitables.add(Specification.instance.unitTypes.getById(UnitType.FREE_COLONIST));

		// when
    	dutch.addFoundingFathers(game, williamBrewster);

		// then
    	assertThat(dutch.getEurope().getRecruitables())
    		.extracting(UnitType::getId)
    		.doesNotContain("model.unit.indenturedServant", "model.unit.pettyCriminal");
	}

}
