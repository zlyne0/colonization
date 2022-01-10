package promitech.colonization.ai;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.TransportUnitMission;

public interface TransportUnitNoDisembarkAccessNotification {
    void noDisembarkAccessNotification(
        PlayerMissionsContainer playerMissionsContainer,
        TransportUnitMission transportUnitMission,
        Tile unitDestination,
        Unit unit
    );
}
