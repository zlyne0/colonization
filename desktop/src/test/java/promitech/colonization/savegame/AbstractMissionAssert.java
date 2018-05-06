package promitech.colonization.savegame;

import org.assertj.core.api.AbstractAssert;

import net.sf.freecol.common.model.ai.missions.AbstractMission;

class AbstractMissionAssert extends AbstractAssert<AbstractMissionAssert, AbstractMission> {

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
	
	public AbstractMissionAssert hasDependMission(String missionId, Class<? extends AbstractMission> missionTypeClass) {
		isNotNull();
		AbstractMission dependMission = actual.getDependMissionById(missionId);
		if (dependMission == null) {
			failWithMessage("Expected depend mission id: %s on Mission id: %s ", missionId, actual.getId());
		} else {
			if (dependMission.getClass() != missionTypeClass) {
				failWithMessage("Expected depend mission type %s on Mission id: %s ", missionTypeClass.getName(), missionId);
			}
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
}