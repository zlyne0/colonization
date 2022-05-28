package net.sf.freecol.common.model.ai.missions;

import net.sf.freecol.common.model.Unit;

import org.assertj.core.api.AbstractAssert;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerMissionsContainerAssert extends AbstractAssert<PlayerMissionsContainerAssert, PlayerMissionsContainer> {

	public PlayerMissionsContainerAssert(PlayerMissionsContainer actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static PlayerMissionsContainerAssert assertThat(PlayerMissionsContainer container) {
		return new PlayerMissionsContainerAssert(container, PlayerMissionsContainerAssert.class);
	}

	public PlayerMissionsContainerAssert hasMission(Class<? extends AbstractMission> missionType, Unit unit) {
		if (actual.findMissions(missionType, unit).size() == 0) {
			failWithMessage("expected player <%s> has mission <%s> for unitId <%s>", actual.getPlayer(), missionType, unit);
		}
		return this;
	}

	public PlayerMissionsContainerAssert hasMission(Class<? extends AbstractMission> missionType, int count) {
		List<? extends AbstractMission> missions = actual.findMissions(missionType);
		if (missions.size() != count) {
			failWithMessage("expected player <%s> has <%d> missions <%s> but it has %d", actual.getPlayer().getId(), count, missionType, missions.size());
		}
		return this;
	}

	public PlayerMissionsContainerAssert hasDependMission(
		AbstractMission mission,
		String dependMissionId,
		Class<? extends AbstractMission> dependMissionTypeClass
	) {
		isNotNull();
		if (!mission.hasDependMission(dependMissionId)) {
			failWithMessage("Expected mission id: <%s> has depend mission: %s ", mission.getId(), dependMissionId);
		} else {
			AbstractMission dependMission = actual.getMission(dependMissionId);
			if (dependMission.getClass() != dependMissionTypeClass) {
				failWithMessage("Expected depend mission type %s on Mission id: %s ", dependMissionTypeClass.getName(), dependMissionId);
			}
		}
		return this;
	}

	public PlayerMissionsContainerAssert hasDependMission(
		String missionId,
		String dependMissionId,
		Class<? extends AbstractMission> dependMissionTypeClass
	) {
		AbstractMission mission = actual.getMission(missionId);
		return hasDependMission(mission, dependMissionId, dependMissionTypeClass);
	}

	public PlayerMissionsContainerAssert doesNotHaveMission(@NotNull AbstractMission mission) {
		if (actual.getMissions().containsId(mission)) {
			failWithMessage("Expected mission id: <%s> not exists in container", mission.getId());
		}
		return this;
	}

	public PlayerMissionsContainerAssert isDone(@NotNull AbstractMission mission) {
		if (!mission.isDone()) {
			failWithMessage("Expected mission id: %s is done, but it's not", mission.getId());
		}
		return this;
	}

}