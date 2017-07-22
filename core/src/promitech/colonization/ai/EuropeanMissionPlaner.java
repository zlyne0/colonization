package promitech.colonization.ai;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.player.Player;

public class EuropeanMissionPlaner {

	public void xxx(Player player) {
		// create missions
		MapIdEntities<ExplorerMission> missions = new MapIdEntities<ExplorerMission>();
		for (Unit unit : player.units.entities()) {
			if (unit.isNaval() && unit.getTileLocationOrNull() != null) {
				missions.add(new ExplorerMission(unit));
			}
		}
	}
	
}
