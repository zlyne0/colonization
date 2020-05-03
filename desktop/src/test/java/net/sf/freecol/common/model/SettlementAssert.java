package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

public class SettlementAssert extends AbstractAssert<SettlementAssert, Settlement> {

	public SettlementAssert(Settlement actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static SettlementAssert assertThat(Settlement settlement) {
		return new SettlementAssert(settlement, SettlementAssert.class);
	}

	public SettlementAssert hasGoods(String goodsTypeId, int amount) {
		int actualAmount = actual.goodsContainer.goodsAmount(goodsTypeId);
		if (actualAmount != amount) {
			failWithMessage("expected goods type %s and amount %s but gets %s",
				goodsTypeId, amount,
				actualAmount
			);
		}
		return this;
	}
}
