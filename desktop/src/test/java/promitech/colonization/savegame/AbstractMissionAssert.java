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
	
	public AbstractMissionAssert isIdEquals(String id) {
		isNotNull();
		if (!actual.getId().equals(id)) {
			failWithMessage("Expected id: %s on AbstractMission id: %s ", id, actual.getId());			
		}
		return this;
	}
	
	public AbstractMissionAssert hasDependMission(String missionId) {
		isNotNull();
		if (!actual.hasDependMission(missionId)) {
			failWithMessage("Expected depend mission id: %s on Mission id: %s ", missionId, actual.getId());
		}
		return this;
	}
	
	public AbstractMissionAssert isType(Class<? extends AbstractMission> missionTypeClass) {
		isNotNull();
		if (actual.getClass() != missionTypeClass) {
			failWithMessage("Expected mission type %s on mission id: %s ", missionTypeClass.getName(), actual.getId());
		}
		return this;
	}

	public AbstractMissionAssert isDone() {
		isNotNull();
		if (!actual.isDone()) {
			failWithMessage("Expected mission id: %s is done, but it's not", actual.getId());
		}
		return this;
	}
}