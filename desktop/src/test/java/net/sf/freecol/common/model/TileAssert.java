package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

public class TileAssert extends AbstractAssert<TileAssert, Tile> {

	public TileAssert(Tile actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static TileAssert assertThat(Tile tile) {
		return new TileAssert(tile, TileAssert.class);
	}

	public TileAssert hasNotUnit(Unit unit) {
		isNotNull();
		if (actual.getUnits().containsId(unit)) {
			failWithMessage("expected no unit <%s> on tile %s, [%d, %d]", unit.getId(), 
				actual.id, actual.x, actual.y
			);
		}
		return this;
	}
	
}
