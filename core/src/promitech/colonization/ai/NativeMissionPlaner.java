package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.WanderMission;
import net.sf.freecol.common.model.player.Player;

public class NativeMissionPlaner {

	private final List<Unit> units = new ArrayList<Unit>();
	
	public void prepareIndianWanderMissions(Player player, PlayerMissionsContainer missionsContainer) {
		for (Settlement settlement : player.settlements.entities()) {
			IndianSettlement tribe = (IndianSettlement)settlement;
			
			units.clear();
			for (Unit tribeUnit : tribe.units) {
				if (!missionsContainer.isUnitBlockedForMission(tribeUnit)) {
					units.add(tribeUnit);
				}
			}
			
			if (units.size() > tribe.settlementType.getMinimumSize() - 1) {
				for (int i = tribe.settlementType.getMinimumSize() - 1; i < units.size(); i++) {
					WanderMission wanderMission = new WanderMission(units.get(i));
					missionsContainer.blockUnitsForMission(wanderMission);
					missionsContainer.addMission(wanderMission);
				}
			}
		}
	}
	
	
}
