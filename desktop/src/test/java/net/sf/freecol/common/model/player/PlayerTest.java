package net.sf.freecol.common.model.player;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.FoundingFather;
import promitech.colonization.savegame.SaveGameParser;

public class PlayerTest {
	
    @BeforeAll
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }
	
	@Test
	public void canDetermineWhetherRecruitUnitWithWilliamBrewster() throws Exception {
		// given
        Game game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
		
        Player player = game.players.getById("player:1");
        player.getFeatures().clear();
        player.getFeatures().addFeaturesAndOverwriteExisted(player.nationType());

        FoundingFather williamBrewster = Specification.instance.foundingFathers.getById("model.foundingFather.williamBrewster");
		player.addFoundingFathers(williamBrewster);
        
        UnitType indenturedServant = Specification.instance.unitTypes.getById("model.unit.indenturedServant");
        UnitType fisherman = Specification.instance.unitTypes.getById("model.unit.expertFisherman");
        
		// when
        boolean canRecruitIndenturedServant = player.getFeatures().canApplyAbilityToObject(Ability.CAN_RECRUIT_UNIT, indenturedServant);
        boolean canRecruitFisherman = player.getFeatures().canApplyAbilityToObject(Ability.CAN_RECRUIT_UNIT, fisherman);
		
		// then
        assertFalse(canRecruitIndenturedServant);
        assertTrue(canRecruitFisherman);
	}

	@Test
	public void canDetermineWhetherRecruitUnitWithoutWilliamBrewster() throws Exception {
		// given
        Game game = SaveGameParser.loadGameFormClassPath("maps/savegame_1600_for_jtests.xml");
		
        Player player = game.players.getById("player:1");
        player.getFeatures().clear();
        player.getFeatures().addFeaturesAndOverwriteExisted(player.nationType());

        UnitType indenturedServant = Specification.instance.unitTypes.getById("model.unit.indenturedServant");
        UnitType fisherman = Specification.instance.unitTypes.getById("model.unit.expertFisherman");
        
        // when
        boolean canRecruitIndenturedServant = player.getFeatures().canApplyAbilityToObject(Ability.CAN_RECRUIT_UNIT, indenturedServant);
        boolean canRecruitFisherman = player.getFeatures().canApplyAbilityToObject(Ability.CAN_RECRUIT_UNIT, fisherman);
        
        // then
        assertTrue(canRecruitIndenturedServant);
        assertTrue(canRecruitFisherman);
	}
}
