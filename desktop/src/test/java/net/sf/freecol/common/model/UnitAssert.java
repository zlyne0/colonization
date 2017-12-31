package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

public class UnitAssert extends AbstractAssert<UnitAssert, Unit> {

	public UnitAssert(Unit actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static UnitAssert assertThat(Unit unit) {
		return new UnitAssert(unit, UnitAssert.class);
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

}
