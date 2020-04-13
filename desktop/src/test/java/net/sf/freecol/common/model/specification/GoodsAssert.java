package net.sf.freecol.common.model.specification;

import org.assertj.core.api.AbstractAssert;

public class GoodsAssert extends AbstractAssert<GoodsAssert, Goods> {

	public GoodsAssert(Goods actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static GoodsAssert assertThat(Goods goods) {
		return new GoodsAssert(goods, GoodsAssert.class);
	}

	public GoodsAssert isEquals(String goodsTypeId, int amount) {
		if (!(actual.getType().equalsId(goodsTypeId) && actual.getAmount() == amount)) {
			failWithMessage("expected goods type %s and amount %s but gets %s:%s",
				goodsTypeId, amount,
				actual.getType().getId(), actual.getAmount()
			);
		}
		return this;
	}
}
