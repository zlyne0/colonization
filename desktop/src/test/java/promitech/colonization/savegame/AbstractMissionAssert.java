package promitech.colonization.savegame;

import org.assertj.core.api.AbstractAssert;

import net.sf.freecol.common.model.ai.missions.AbstractMission;

public class AbstractMissionAssert extends AbstractAssert<AbstractMissionAssert, AbstractMission> {

	public AbstractMissionAssert(AbstractMission actual, Class<?> selfType) {
		super(actual, selfType);
	}
	
	public static AbstractMissionAssert assertThat(AbstractMission abstractMission) {
		return new AbstractMissionAssert(abstractMission, AbstractMissionAssert.class);
	}

	public AbstractMissionAssert isDone() {
		isNotNull();
		if (!actual.isDone()) {
			failWithMessage("Expected mission id: %s is done, but it's not", actual.getId());
		}
		return this;
	}

	public AbstractMissionAssert isNotDone() {
		isNotNull();
		if (actual.isDone()) {
			failWithMessage("Expected mission id: %s is not done, but it's done", actual.getId());
		}
		return this;
	}

	public AbstractMissionAssert hasNotDependMission() {
		if (actual.hasDependMission()) {
			failWithMessage("Expected mission id: %s has not depend mission, but it has", actual.getId());
		}
		return this;
	}
}