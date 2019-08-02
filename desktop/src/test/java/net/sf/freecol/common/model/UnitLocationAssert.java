package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

public class UnitLocationAssert extends AbstractAssert<UnitLocationAssert, UnitLocation> {

	private UnitLocationAssert(UnitLocation actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static UnitLocationAssert assertThat(UnitLocation unitLocation) {
		return new UnitLocationAssert(unitLocation, UnitLocationAssert.class);
	}

	public UnitLocationAssert hasSize(int count) {
	    if (actual.getUnits().size() != count) {
	        failWithMessage("expect unit location <%s> has <%d> units, but has <%d>", actual, count, actual.getUnits().size());
	    }
	    return this;
	}
	
	public UnitLocationAssert hasUnit(String unitId) {
	    if (!actual.getUnits().containsId(unitId)) {
	        failWithMessage("expect unit location <%s> contains unitId <%s>, but it's not", actual, unitId);
	    }
	    return this;
	}
	
	public UnitLocationAssert hasUnitType(String unitTypeId) {
	    boolean found = false;
	    for (Unit unit : actual.getUnits().entities()) {
            if (unit.unitType.equalsId(unitTypeId)) {
                found = true;
            }
        }
	    if (!found) {
	        failWithMessage("expect unit location <%s> contains unit with tyle <%s>, but it's not", actual, unitTypeId);
	    }
	    return this;
	}
    
}
