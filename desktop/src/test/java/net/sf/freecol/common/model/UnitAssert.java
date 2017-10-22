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

	public UnitAssert notExistsOnTile(Tile tile) {
		isNotNull();
		if (tile.getUnits().containsId(actual)) {
			failWithMessage("expected no unit <%s> on tile %s, [%d, %d]", actual.getId(), 
				tile.id, tile.x, tile.y
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
}
