package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

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
	    if (actual.settlementWorkers().size() != size) {
	        failWithMessage("expected colony <%s> has size <%d> but has <%d>", actual.getId(), size, actual.settlementWorkers().size());
	    }
	    return this;
	}
    
}
