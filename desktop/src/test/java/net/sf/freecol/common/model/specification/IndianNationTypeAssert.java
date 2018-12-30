package net.sf.freecol.common.model.specification;

import org.assertj.core.api.AbstractAssert;

import net.sf.freecol.common.model.UnitType;

public class IndianNationTypeAssert extends AbstractAssert<IndianNationTypeAssert, IndianNationType> {

	public IndianNationTypeAssert(IndianNationType actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static IndianNationTypeAssert assertThat(IndianNationType nationType) {
		return new IndianNationTypeAssert(nationType, IndianNationTypeAssert.class);
	}
	
	public IndianNationTypeAssert hasSkill(String unitTypeId, int probability) {
		boolean found = false;
		for (RandomChoice<UnitType> randomChoice : actual.getSkills()) {
			if (randomChoice.probabilityObject().equalsId(unitTypeId) && randomChoice.getOccureProbability() == probability) {
				found = true;
				break;
			}
		}
		if (!found) {
			failWithMessage(
				"indian nation type %s expected skill %s with probability %s but not found",
				actual.getId(), unitTypeId, probability
			);
		}
		return this;
	}
}
