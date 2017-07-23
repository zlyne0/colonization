package promitech.colonization.ai;

import net.sf.freecol.common.model.ai.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.AbstractMission;

public interface MissionHandler<T extends AbstractMission> {
    void handle(PlayerMissionsContainer playerMissionsContainer, T mission);
}
