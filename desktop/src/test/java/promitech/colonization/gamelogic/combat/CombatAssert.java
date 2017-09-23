package promitech.colonization.gamelogic.combat;

import org.assertj.core.api.AbstractAssert;

public class CombatAssert extends AbstractAssert<CombatAssert, Combat>  {

	private static final float COMPARE_OFFSET = 0.01f;

	public CombatAssert(Combat actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static CombatAssert assertThat(Combat combat) {
		return new CombatAssert(combat, CombatAssert.class);
	}
	
	public CombatAssert hasPowers(float offencePower, float defencePower, float winPropability) {
		isNotNull();
		
		if (Math.abs(actual.getOffencePower() - offencePower) >= COMPARE_OFFSET
				|| Math.abs(actual.getDefencePower() - defencePower) >= COMPARE_OFFSET
				|| Math.abs(actual.getWinPropability() - winPropability) >= COMPARE_OFFSET) {
			
			failWithMessage("expected powers <offence: %s, defence: %s, winProbability: %s> but got\n"
					+ " <offence: %s, defence: %s, winProbability: %s>", 
					offencePower, defencePower, winPropability,
					actual.getOffencePower(), actual.getDefencePower(), actual.getWinPropability());
		}
		return this;
	}
	
}
