package promitech.colonization.ai;

import net.sf.freecol.common.model.Building;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.colonyproduction.ColonyPlan;
import net.sf.freecol.common.model.colonyproduction.MaxGoodsProductionLocation;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.BuildingType;

public final class ColonyProductionPlaner {

	private ColonyProductionPlaner() {
	}
	
	public static void createPlan(Player player, PlayerMissionsContainer playerMissionContainer) {
		for (Settlement settlement : player.settlements.entities()) {
			new ColonyPlan(settlement.asColony())
				.withIgnoreIndianOwner()
				.withConsumeWarehouseResources(true)
				.withMinimumProductionLimit(2)
				.executeMaximizationProduction(ColonyPlan.Plan.MostValuable, ColonyPlan.Plan.Bell, ColonyPlan.Plan.Food);
		}
	}
	
	public static void createPlan(Colony colony) {
		new ColonyPlan(colony)
			.withIgnoreIndianOwner()
			.withConsumeWarehouseResources(true)
			.withMinimumProductionLimit(2)
			.executeMaximizationProduction(ColonyPlan.Plan.MostValuable, ColonyPlan.Plan.Bell, ColonyPlan.Plan.Food);
	}

	public static void initColonyBuilderUnit(Colony colony, Unit builder) {
		colony.updateModelOnWorkerAllocationOrGoodsTransfer();

		MaxGoodsProductionLocation maxProd = colony.productionSimulation().determinePotentialMaxTilesProduction(builder.unitType, false);
		if (maxProd != null) {
			colony.addWorkerToTerrain(maxProd.getColonyTile(), builder);
		} else {
			addUnitToRandomBuilding(colony, builder);
		}
		colony.updateColonyPopulation();
	}

	private static void addUnitToRandomBuilding(Colony colony, Unit unit) {
		Building townHall = colony.findBuildingByType(BuildingType.TOWN_HALL);
		if (townHall.canAddWorker(unit.unitType)) {
			colony.addWorkerToBuilding(townHall, unit);
		} else {
			for (Building building : colony.buildings) {
				if (building.canAddWorker(unit.unitType)) {
					colony.addWorkerToBuilding(building, unit);
					break;
				}
			}
		}
	}


}
