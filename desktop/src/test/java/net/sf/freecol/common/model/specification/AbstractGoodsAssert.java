package net.sf.freecol.common.model.specification;

import org.assertj.core.api.AbstractAssert;

public class AbstractGoodsAssert extends AbstractAssert<AbstractGoodsAssert, AbstractGoods> {

	public AbstractGoodsAssert(AbstractGoods actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static AbstractGoodsAssert assertThat(AbstractGoods goods) {
		return new AbstractGoodsAssert(goods, AbstractGoodsAssert.class);
	}

	public AbstractGoodsAssert isEquals(String goodsTypeId, int amount) {
		if (!(actual.getTypeId().equals(goodsTypeId) && actual.getQuantity() == amount)) {
			failWithMessage("expected goods type %s and amount %s but gets %s:%s",
				goodsTypeId, amount,
				actual.getTypeId(), actual.getQuantity()
			);
		}
		return this;
	}
}
