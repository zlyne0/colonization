package promitech.colonization.ai;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;

public interface TransportUnitNoDisembarkAccessNotification {
    void noDisembarkAccessNotification(
        TransportUnitMission transportUnitMission,
        Tile unitDestination,
        AbstractMission parentMission
    );
}
