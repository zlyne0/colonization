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
	        if (building.workers.containsId(notContainedUnit)) {
	            failWithMessage("expected colony <%s> does not contain unit <%s> but building <%s> contains", actual.getId(), notContainedUnit.getId(), building.getId());
	        }
        }
	    for (ColonyTile colonyTile : actual.colonyTiles.entities()) {
	        if (colonyTile.getWorker() != null && colonyTile.getWorker().equalsId(notContainedUnit)) {
                failWithMessage("expected colony <%s> does not contain unit <%s> but colony tile <%s> contains", actual.getId(), notContainedUnit.getId(), colonyTile.getId());
	        }
        }
	    return this;
	}
	
    
}
