package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

import net.sf.freecol.common.model.player.Player;
import promitech.map.isometric.NeighbourIterableTile;

public class UnitAssert extends AbstractAssert<UnitAssert, Unit> {

	public UnitAssert(Unit actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static UnitAssert assertThat(Unit unit) {
		return new UnitAssert(unit, UnitAssert.class);
	}

	public UnitAssert isIdEquals(String id) {
		if (!actual.equalsId(id)) {
			failWithMessage("expected unit <%s> id is equals <%s>", actual.getId(), id);
		}
		return this;
	}
	
	public UnitAssert isDisposed() {
		isNotNull();
		if (actual.getOwner().units.containsId(actual)) {
			failWithMessage("expected player <%s> has not unit <%s>", actual.getOwner().getId(), actual.getId());
		}
		if (actual.location != null) {
			failWithMessage("expected unit <%s> has no location", actual.getId());
		}
		if (!actual.isDisposed()) {
			failWithMessage("expected unit <%s> to be disposed", actual.getId());
		}
		return this;
	}

    public UnitAssert isNotDisposed() {
        isNotNull();
        if (actual.isDisposed()) {
            failWithMessage("expected unit <%s> to be not disposed", actual.getId());
        }
        return this;
    }
	
	public UnitAssert notExistsOnTile(Tile tile) {
		isNotNull();
		if (tile.getUnits().containsId(actual)) {
			failWithMessage("expected no unit <%s> on tile %s, [%d, %d]", actual.getId(), 
				tile.id, tile.x, tile.y
			);
		}
		return this;
	}
	
	public UnitAssert isExistsOnTile(Tile tile) {
		isNotNull();
		if (!tile.getUnits().containsId(actual)) {
			failWithMessage("expected unit <%s> on tile %s, [%d, %d]", actual.getId(), 
				tile.id, tile.x, tile.y
			);
		}
		return this;
	}
	
	public UnitAssert isAtLocation(Class<? extends UnitLocation> unitLocationClass) {
	    if (!actual.isAtLocation(unitLocationClass)) {
	        failWithMessage(
	            "expected unit <%s> to be at location <%s> but is is at location <%s>", 
	            actual.getId(), unitLocationClass, actual.location
            );
	    }
	    return this;
	}
	
	public UnitAssert isAtLocation(UnitLocation unitLocation) {
		if (actual.location != unitLocation || !unitLocation.getUnits().containsId(actual)) {
			failWithMessage("expected unit <%s> to be at location <%s> but it is at location <%s>",
				actual.getId(),
				unitLocation,
				actual.location
			);
		}
		return this;
	}

	public UnitAssert isNextToLocation(Tile location) {
		isNotNull();
		Tile unitLocation = actual.getTileLocationOrNull();
		if (unitLocation == null) {
			failWithMessage(
				"expected unit <%s> to be next to tile <%s> but it is not on tile", 
				actual.getId(), 
				location
			);
			return this;
		}
		
		if (!unitLocation.isStepNextTo(location)) {
			failWithMessage("expected unit <%s> to be next to tile <%s>", actual.getId(), location);
		}
		return this;
	}
	
	public UnitAssert isNotAtLocation(UnitLocation unitLocation) {
		if (actual.location == unitLocation || unitLocation.getUnits().containsId(actual)) {
			failWithMessage("expected unit <%s> not to be at location <%s> ",
				actual.getId(), 
				unitLocation
			);
		}
		return this;
	}
	
	public UnitAssert hasGoods(String goodsId, int amount) {
		isNotNull();
		if (!actual.getGoodsContainer().hasGoodsQuantity(goodsId, amount)) {
			failWithMessage("unit <%s> has not cargo <%s> in amount <%d>", 
				actual.getId(), goodsId, amount
			);
		}
		return this;
	}

    public UnitAssert hasNoGoods() {
        isNotNull();
        if (actual.getGoodsContainer().getCargoSpaceTaken() != 0) {
            failWithMessage("expected unit <%s> to has no cargo", actual.getId());
        }
        return this;
    }

    public UnitAssert hasNoUnits() {
        isNotNull();
        if (actual.getUnitContainer().isNotEmpty()) {
            failWithMessage("expected unit <%s> to carry no unit", actual.getId());
        }
        return this;
    }

	public UnitAssert hasUnit(Unit unit) {
		isNotNull();
		if (!actual.getUnitContainer().getUnits().containsId(unit)) {
            failWithMessage("expected unit <%s> to carry unit <%s>", actual.getId(), unit.getId());
		}
		return this;
	}

	public UnitAssert hasUnitsSize(int size) {
		int actualSize = actual.getUnitContainer().getUnits().size();
		if (actualSize != size) {
			failWithMessage(
				"expected unit <%s> has <%s> units in cargo but has <%s>",
				actual.getId(), size, actualSize
			);
		}
		return this;
	}

    public UnitAssert isDamaged() {
        if (!actual.isDamaged()) {
            failWithMessage("expected unit <%s> to be damaged", actual.getId());
        }
        return this;
    }

    public UnitAssert hasNoMovesPoints() {
        if (actual.hasMovesPoints()) {
            failWithMessage("expected unit <%s> to has no moves points", actual.getId());
        }
        return this;
    }

    public UnitAssert isUnitType(String unitTypeId) {
        if (!actual.unitType.equalsId(unitTypeId)) {
            failWithMessage("expected unit <%s> to be unit type <%s> but it is <%s>", actual.getId(), unitTypeId, actual.unitType.getId());
        }
        return this;
    }
    
    public UnitAssert isUnitType(UnitType unitType) {
        return this.isUnitType(unitType.getId());
    }

    public UnitAssert isUnitRole(String unitRoleId) {
        if (!actual.unitRole.equalsId(unitRoleId)) {
            failWithMessage("expected unit <%s> to be in role <%s> but it is in <%s>", actual.getId(), unitRoleId, actual.unitRole.getId());
        }
        return this;
    }

    public UnitAssert isOwnedBy(Player player) {
        if (!actual.getOwner().equalsId(player)) {
            failWithMessage("expected unit <%s> owned by <%p> but it is owned by <%s>", actual.getId(), player.getId(), actual.getOwner().getId());
        }
        if (!player.units.containsId(actual)) {
            failWithMessage("expected that player <%s> posses unit <%s>", player.getId(), actual.getId());
        }
        return this;
    }
}
