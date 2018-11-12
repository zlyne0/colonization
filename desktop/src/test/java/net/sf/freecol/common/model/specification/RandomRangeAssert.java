package net.sf.freecol.common.model.specification;

import org.assertj.core.api.AbstractAssert;

public class RandomRangeAssert extends AbstractAssert<RandomRangeAssert, RandomRange> {

	public RandomRangeAssert(RandomRange actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static RandomRangeAssert assertThat(RandomRange range) {
		return new RandomRangeAssert(range, RandomRangeAssert.class);
	}

	public RandomRangeAssert equalsProbMinMaxFactor(
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
