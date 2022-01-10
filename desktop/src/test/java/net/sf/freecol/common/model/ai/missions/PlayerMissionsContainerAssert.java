package net.sf.freecol.common.model.ai.missions;

import net.sf.freecol.common.model.Unit;

import org.assertj.core.api.AbstractAssert;

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
}
