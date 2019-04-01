package net.sf.freecol.common.model.player;

import static org.assertj.core.api.Assertions.*;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.savegame.SaveGameParser;

public class PlayerTest {
	
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }

    Game game;
    Player dutch;
    FoundingFather thomasPaine;
    
    @BeforeEach
    public void setup() throws IOException, ParserConfigurationException, SAXException {
    	game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
    	dutch = game.players.getById("player:1");
		thomasPaine = Specification.instance.foundingFathers.getById("model.foundingFather.thomasPaine");
    }
    
	@Test
	public void canDetermineWhetherRecruitUnitWithWilliamBrewster() throws Exception {
		// given
		
        dutch.getFeatures().clear();
        dutch.getFeatures().addFeaturesAndOverwriteExisted(dutch.nationType());

        FoundingFather williamBrewster = Specification.instance.foundingFathers.getById("model.foundingFather.williamBrewster");
		dutch.addFoundingFathers(game, williamBrewster);
        
        UnitType indenturedServant = Specification.instance.unitTypes.getById("model.unit.indenturedServant");
        UnitType fisherman = Specification.instance.unitTypes.getById("model.unit.expertFisherman");
        
		// when
        boolean canRecruitIndenturedServant = dutch.getFeatures().canApplyAbilityToObject(Ability.CAN_RECRUIT_UNIT, indenturedServant);
        boolean canRecruitFisherman = dutch.getFeatures().canApplyAbilityToObject(Ability.CAN_RECRUIT_UNIT, fisherman);
		
		// then
        assertThat(canRecruitIndenturedServant).isFalse();
        assertThat(canRecruitFisherman).isTrue();
	}

	@Test
	public void canDetermineWhetherRecruitUnitWithoutWilliamBrewster() throws Exception {
		// given
        dutch.getFeatures().clear();
        dutch.getFeatures().addFeaturesAndOverwriteExisted(dutch.nationType());

        UnitType indenturedServant = Specification.instance.unitTypes.getById("model.unit.indenturedServant");
        UnitType fisherman = Specification.instance.unitTypes.getById("model.unit.expertFisherman");
        
        // when
        boolean canRecruitIndenturedServant = dutch.getFeatures().canApplyAbilityToObject(Ability.CAN_RECRUIT_UNIT, indenturedServant);
        boolean canRecruitFisherman = dutch.getFeatures().canApplyAbilityToObject(Ability.CAN_RECRUIT_UNIT, fisherman);
        
        // then
        assertThat(canRecruitIndenturedServant).isTrue();
        assertThat(canRecruitFisherman).isTrue();
	}
	
	@Test
	public void thomasPaineCanRiseBellProduction() throws Exception {
		// given
        if (!dutch.foundingFathers.containsId(thomasPaine)) {
        	dutch.addFoundingFathers(game, thomasPaine);
        }
        dutch.setTax(20);
		
		// when
		int bellProduction = (int)dutch.getFeatures().applyModifier(GoodsType.BELLS, 100);

		// then
		assertThat(bellProduction).isEqualTo(120);
	}
	
	@Test
	public void withoutThomasPaineNoTaxRiseBellProduction() throws Exception {
		// given
		if (dutch.foundingFathers.containsId(thomasPaine)) {
			fail("should has not " + thomasPaine);
		}
		dutch.setTax(20);
		
		// when
		int bellProduction = (int)dutch.getFeatures().applyModifier(GoodsType.BELLS, 100);

		// then
		assertThat(bellProduction).isEqualTo(100);
	}
}
