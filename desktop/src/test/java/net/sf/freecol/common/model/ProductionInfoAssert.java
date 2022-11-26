package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

public class ProductionInfoAssert extends AbstractAssert<ProductionInfoAssert, ProductionInfo> {

	private ProductionInfoAssert(ProductionInfo actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static ProductionInfoAssert assertThat(ProductionInfo productionInfo) {
		return new ProductionInfoAssert(productionInfo, ProductionInfoAssert.class);
	}

	public ProductionInfoAssert hasOutput(String goodsTypeId) {
		isNotNull();
		
		boolean found = false;
		for (Production production : actual.productions) {
			if (production.isOutputTypesEquals(goodsTypeId)) {
				found = true;
			}
		}
		if (!found) {
			failWithMessage("can not find production goodsType <%s> but has <%s>", goodsTypeId, actual);
		}
		return this;
	}
	
	public ProductionInfoAssert hasOutput(String goodsTypeId, int amount) {
		return hasOutput(goodsTypeId, amount, false);
	}
	
	public ProductionInfoAssert hasOutput(String goodsTypeId, int amount, boolean unattended) {
		isNotNull();
		
		boolean found = false;
		for (Production production : actual.productions) {
			if (production.isOutputTypesEquals(goodsTypeId, amount) && production.isUnattended() == unattended) {
				found = true;
			}
		}
		if (!found) {
			failWithMessage("can not find production goodsType <%s> in amount <%d> but has <%s>", goodsTypeId, amount, actual);
		}
		return this;
	}
	
}
