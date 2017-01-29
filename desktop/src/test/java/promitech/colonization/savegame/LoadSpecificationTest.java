package promitech.colonization.savegame;

import static org.assertj.core.api.Assertions.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.player.ArmyForceAbstractUnit;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.Modifier;
import net.sf.freecol.common.model.specification.NationType;
import net.sf.freecol.common.model.specification.NationType.SettlementNumber;
import net.sf.freecol.common.model.specification.options.UnitListOption;

public class LoadSpecificationTest {

    @BeforeClass
    public static void beforeClass() {
        Gdx.files = new LwjglFiles();
    }

    @Test
	public void canLoadSpecificationConfigFile() throws Exception {
		// given
    	
    	// when
    	SaveGameParser.loadDefaultSpecification();
    	Specification.instance.updateOptionsFromDifficultyLevel("model.difficulty.veryEasy");
    	
    	// then
    	Specification specification = Specification.instance;
    	verifyNationType(specification);
    	verifyUnitTypes(specification);
    	verifyOptions();
	}

	private void verifyOptions() {
		UnitListOption unitListOption = Specification.options.getUnitListOption(GameOptions.REF_FORCE);
    	assertThat(unitListOption).isNotNull();
    	assertThat(unitListOption.unitOptions.getById("model.option.refSize.soldiers")).isNotNull();
    	assertThat(unitListOption.unitOptions.getById("model.option.refSize.dragoons")).isNotNull();
    	assertThat(unitListOption.unitOptions.getById("model.option.refSize.artillery")).isNotNull();
    	assertThat(unitListOption.unitOptions.getById("model.option.refSize.menOfWar")).isNotNull();
    	
    	ArmyForceAbstractUnit soldiersArmy = unitListOption.unitOptions.getById("model.option.refSize.soldiers").createArmyForce();
    	assertThat(soldiersArmy.getAmount()).isEqualTo(15);
    	assertThat(soldiersArmy.getUnitType().getId()).isEqualTo("model.unit.kingsRegular");
    	assertThat(soldiersArmy.getUnitRole().getId()).isEqualTo("model.role.infantry");
	}

	private void verifyUnitTypes(Specification specification) {
		UnitType caravel = specification.unitTypes.getById("model.unit.caravel");
    	assertThat(caravel.getPrice()).isEqualTo(1000);
    	assertThat(caravel.hasAbility(Ability.NAVAL_UNIT)).isTrue();
    	assertThat(caravel.applyModifier(Modifier.TRADE_VOLUME_PENALTY, 100)).isEqualTo(25);
    	
    	UnitType freeColonist = specification.unitTypes.getById(UnitType.FREE_COLONIST);
    	assertThat(freeColonist.unitConsumption.getById(GoodsType.FOOD).getQuantity()).isEqualTo(2);
    	assertThat(freeColonist.unitConsumption.getById(GoodsType.BELLS).getQuantity()).isEqualTo(1);
	}

	private void verifyNationType(Specification specification) {
		NationType iroquoisNationType = specification.nationTypes.getById("model.nationType.iroquois");
    	assertThat(iroquoisNationType.getSettlementNumber()).isEqualTo(SettlementNumber.AVERAGE);
    	assertThat(iroquoisNationType.getSettlementCapitalType().getClaimableRadius()).isEqualTo(2);
	}
	
}
