package promitech.colonization.ai;

import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;

public interface MissionHandler<T extends AbstractMission> {
    void handle(PlayerMissionsContainer playerMissionsContainer, T mission);
}
