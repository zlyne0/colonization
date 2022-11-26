package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

public class ProductionAssert extends AbstractAssert<ProductionAssert, Production> {

	private ProductionAssert(Production actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static ProductionAssert assertThat(Production production) {
		return new ProductionAssert(production, ProductionAssert.class);
	}

	public ProductionAssert hasOutput(String goodsTypeId) {
		isNotNull();
		if (!actual.isOutputTypesEquals(goodsTypeId)) {
			failWithMessage("can not find production goodsType <%s> but has <%s>", goodsTypeId, actual);
		}
		return this;
	}
	
	public ProductionAssert hasOutput(String goodsTypeId, int amount) {
		return hasOutput(goodsTypeId, amount, false);
	}
	
	public ProductionAssert hasOutput(String goodsTypeId, int amount, boolean unattended) {
		isNotNull();
		if (!actual.isOutputTypesEquals(goodsTypeId, amount) && actual.isUnattended() == unattended) {
			failWithMessage("can not find production goodsType <%s> in amount <%d> but has <%s>", goodsTypeId, amount, actual);
		}
		return this;
	}
	
}
