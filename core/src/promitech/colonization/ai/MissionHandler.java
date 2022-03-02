package promitech.colonization.ai;

import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;

import org.jetbrains.annotations.NotNull;

public interface MissionHandler<T extends AbstractMission> {
    void handle(@NotNull PlayerMissionsContainer playerMissionsContainer, @NotNull T mission);
}
