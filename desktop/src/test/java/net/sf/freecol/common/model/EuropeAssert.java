package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

public class EuropeAssert extends AbstractAssert<EuropeAssert, Europe> {

	public EuropeAssert(Europe actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static EuropeAssert assertThat(Europe europe) {
		return new EuropeAssert(europe, EuropeAssert.class);
	}

	public EuropeAssert hasUnitPrice(UnitType unitType, int price) {
		int actualPrice = actual.getUnitPrice(unitType);
		if (actualPrice != price) {
			failWithMessage("expected price for unit <%s> is <%s> but it is <%s>", unitType.getId(), price, actualPrice);
		}
		return this;
	}
}
