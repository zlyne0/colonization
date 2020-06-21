package promitech.colonization.ai;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.ai.ColonyPlan;
import net.sf.freecol.common.model.ai.ColonyPlan.Plan;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.player.Player;

public class ColonyProductionPlaner {

	public ColonyProductionPlaner() {
	}
	
	public void createPlan(Player player, PlayerMissionsContainer playerMissionContainer) {
		for (Settlement settlement : player.settlements.entities()) {
			new ColonyPlan(settlement.asColony())
				.withIgnoreIndianOwner()
				.withConsumeWarehouseResources(true)
				.execute2(Plan.MostValuable);
		}
	}
	
	public void createPlan(Colony colony) {
		new ColonyPlan(colony)
			.withIgnoreIndianOwner()
			.withConsumeWarehouseResources(true)
			.execute2(Plan.MostValuable);
	}
	
}
