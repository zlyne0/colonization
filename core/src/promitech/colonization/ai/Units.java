package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.player.Player;

public class Units {

	public static final Comparator<Unit> FREE_CARGO_SPACE_COMPARATOR = new Comparator<Unit>() {
		@Override
		public int compare(Unit o1, Unit o2) {
			if (o2.hasMoreFreeCargoSpace(o1)) {
				return 1;
			} else {
				return -1;
			}
		}
	};
	
    public static Unit findCarrierToRellocateUnit(Unit unit) {
        Unit ship = null;
        for (Unit u : unit.getOwner().units.entities()) {
            if (u.isNaval() && u.isCarrier()) {
                ship = u;
            }
        }
        return ship;
    }

    public static List<Unit> findCarrierTransportResources(Game game, Player player) {
    	PlayerMissionsContainer missionContainer = game.aiContainer.missionContainer(player);
    	List<Unit> units = null;
    	for (Unit unit : player.units) {
			if (missionContainer.isUnitBlockedForMission(unit) || unit.isDamaged()) {
    			continue;
    		}
			if (unit.isNaval() && unit.hasSpaceForAdditionalCargo()) {
				if (units == null) {
					units = new ArrayList<Unit>();
				}
				units.add(unit);
			}
		}
    	if (units != null) {
			Collections.sort(units, FREE_CARGO_SPACE_COMPARATOR);
			return units;
    	} else {
    		return Collections.emptyList(); 
    	}
    }
    
}
