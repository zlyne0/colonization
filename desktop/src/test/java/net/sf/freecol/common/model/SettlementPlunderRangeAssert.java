package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

public class SettlementPlunderRangeAssert extends AbstractAssert<SettlementPlunderRangeAssert, SettlementPlunderRange> {

	public SettlementPlunderRangeAssert(SettlementPlunderRange actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static SettlementPlunderRangeAssert assertThat(SettlementPlunderRange range) {
		return new SettlementPlunderRangeAssert(range, SettlementPlunderRangeAssert.class);
	}

	public SettlementPlunderRangeAssert equalsProbMinMaxFactor(
		int probability, int minimum, 
		int maximum, int factor
	) {
		isNotNull();
		
		if (actual.factor != factor 
				|| actual.maximum != maximum
				|| actual.minimum != minimum
				|| actual.probability != probability) {
			failWithMessage("expected range factor: %s, maximum: %s, minimum: %s, probability: %s but gets <%s>", 
				factor, maximum, minimum, probability,
				actual
			);
		}
		return this;
	}

    
}
