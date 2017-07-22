package promitech.colonization.ai;

import net.sf.freecol.common.model.ai.AbstractMission;

public interface MissionHandler<T extends AbstractMission> {
    void handle(T mission);
}
