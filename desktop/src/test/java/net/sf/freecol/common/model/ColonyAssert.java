package net.sf.freecol.common.model;

import net.sf.freecol.common.model.specification.BuildableType;

import org.assertj.core.api.AbstractAssert;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ColonyAssert extends AbstractAssert<ColonyAssert, Colony> {

	public ColonyAssert(Colony actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static ColonyAssert assertThat(Colony colony) {
		return new ColonyAssert(colony, ColonyAssert.class);
	}

	public ColonyAssert notContainsUnit(Unit notContainedUnit) {
	    for (Building building : actual.buildings.entities()) {
	        if (building.getUnits().containsId(notContainedUnit)) {
	            failWithMessage("expected colony <%s> does not contain unit <%s> but building <%s> contains", actual.getId(), notContainedUnit.getId(), building.getId());
	        }
        }
	    for (ColonyTile colonyTile : actual.colonyTiles.entities()) {
	        if (colonyTile.hasWorker(notContainedUnit)) {
                failWithMessage("expected colony <%s> does not contain unit <%s> but colony tile <%s> contains", actual.getId(), notContainedUnit.getId(), colonyTile.getId());
	        }
        }
	    return this;
	}

	public ColonyAssert hasNotBuilding(String buildingTypeId) {
		if (actual.findBuildingByTypeOrNull(buildingTypeId) != null) {
			failWithMessage("expected colony <%s> does not have building type <%s>", actual.getId(), buildingTypeId);
		}
		return this;
	}

	public ColonyAssert hasBuilding(String buildingTypeId) {
		if (actual.findBuildingByTypeOrNull(buildingTypeId) == null) {
			failWithMessage("expected colony <%s> has building type <%s>", actual.getId(), buildingTypeId);
		}
		return this;
	}

	public ColonyAssert hasSize(int size) {
	    if (actual.getColonyUnitsCount() != size) {
	        failWithMessage("expected colony <%s> has size <%d> but has <%d>", actual.getId(), size, actual.settlementWorkers().size());
	    }
	    return this;
	}

	public ColonyAssert hasWorkerInLocation(String unitLocationId) {
		return hasWorkerInLocation(unitLocationId, 1);
	}
	
	public ColonyAssert hasWorkerInLocation(String unitLocationId, int workersAmount) {
		UnitLocation unitLocation = actual.findUnitLocationById(unitLocationId);
		if (unitLocation == null) {
			failWithMessage(
				"exptected <%s> workers in unitLocationId <%s>, but no unit location",
				workersAmount,
				unitLocationId
			);
		} else {
			if (unitLocation.getUnits().size() < workersAmount) {
				failWithMessage("exptected <%s> workers in unitLocationId <%s>, but it has <%s> workers",
					workersAmount,
					unitLocationId,
					unitLocation.getUnits().size()
				);
			}
		}
		return this;
	}

	public ColonyAssert hasWorkerInLocation(String unitLocationId, String unitTypeId) {
		hasWorkerInLocation(unitLocationId, 1);
		UnitLocationAssert.assertThat(actual.findUnitLocationById(unitLocationId))
		    .hasUnitType(unitTypeId);
		return this;
	}
	
	public ColonyAssert hasWorkerInBuildingType(String buildingTypeId) {
		return hasWorkerInBuildingType(buildingTypeId, 1);
	}

	public ColonyAssert hasNoWorkerInBuildingType(String buildingTypeId) {
		Building building = actual.findBuildingByTypeOrNull(buildingTypeId);
		if (building == null) {
			failWithMessage(
				"exptected no workers in building type <%s> location, but building type not found",
				buildingTypeId
			);
		} else {
			if (building.getUnits().size() > 0) {
				failWithMessage("exptected no workers in building type <%s> location, but it has <%s> workers",
					buildingTypeId,
					building.getUnits().size()
				);
			}
		}
		return this;
	}

	public ColonyAssert hasWorkerInBuildingType(String buildingTypeId, int workersAmount) {
		Building building = actual.findBuildingByTypeOrNull(buildingTypeId);
		if (building == null) {
			failWithMessage(
				"exptected <%s> workers in building type <%s> location, but building type not found",
				workersAmount,
				buildingTypeId
			);
		} else {
			if (building.getUnits().size() < workersAmount) {
				failWithMessage("exptected <%s> workers in building type <%s> location, but it has <%s> workers",
					workersAmount,
					buildingTypeId,
					building.getUnits().size()
				);
			}
		}
		return this;
	}

	public ColonyAssert hasWorkerInBuildingType(String buildingTypeId, String unitTypeId) {
        Building building = actual.findBuildingByTypeOrNull(buildingTypeId);
        if (building == null) {
            failWithMessage(
                "exptected <%s> worker in building type <%s> location, but building type not found",
                unitTypeId,
                buildingTypeId
            );
        } else {
            UnitLocationAssert.assertThat(building)
                .hasUnitType(unitTypeId);
        }
	    return this;
	}
	
    public ColonyAssert produce(String goodsTypeId, int expectedAmount) {
        int amount = actual.productionSummary().getQuantity(goodsTypeId);
        if (amount != expectedAmount) {
            failWithMessage("expected production <%d> of <%s> goods type but has <%d>", expectedAmount, goodsTypeId, amount);
        }
        return this;
    }

	public ColonyAssert hasProductionOnTile(String tileId, String unitTypeId, String goodsTypeId, int amount) {
		hasWorkerInLocation(tileId, unitTypeId);
		ProductionSummaryAssert.assertThat(actual.productionSummary(tileProductionLocation(tileId)).realProduction)
			.has(goodsTypeId, amount);
		return this;
	}

	private ProductionLocation tileProductionLocation(final String tileId) {
		return () -> tileId;
	}

	public ColonyAssert hasProductionOnTile(String tileId, String goodsTypeId, int amount) {
		hasWorkerInLocation(tileId);
		ProductionSummaryAssert.assertThat(actual.productionSummary(tileProductionLocation(tileId)).realProduction)
			.has(goodsTypeId, amount);
		return this;
	}

    public ColonyAssert hasGoods(@NotNull String goodsTypeId, int amount) {
		int actualAmount = actual.getGoodsContainer().goodsAmount(goodsTypeId);
		if (actualAmount != amount) {
			failWithMessage("expected colony %s has %d goods, but it has %d", actual.getId(), amount, actualAmount);
		}
        return this;
    }

	public ColonyAssert hasBuildingQueue(@NotNull String ... buildingTypeIds) {
		if (actual.buildingQueue.size() != buildingTypeIds.length) {
			failWithMessage(
				"expected colony %s has %s building queue but is has %s",
				actual.getId(),
				Arrays.deepToString(buildingTypeIds),
				actual.buildingQueue
			);
		}
		for (int i=0; i<buildingTypeIds.length; i++) {
			if (!actual.buildingQueue.get(i).getType().equalsId(buildingTypeIds[i])) {
				failWithMessage(
					"expected colony %s has %s building queue but is has %s",
					actual.getId(),
					Arrays.deepToString(buildingTypeIds),
					actual.buildingQueue
				);
			}
		}
		return this;
	}

    public ColonyAssert hasCompletedBuilding(@NotNull BuildableType buildableType) {
		BuildProgress buildProgress = BuildProgress.calculateBuildProgress(actual, buildableType);
		if (!buildProgress.isCompleted()) {
			failWithMessage("expected colony <%s> completed build <%s> but it is not", actual.getId(), buildableType);
		}
		return this;
    }
}
