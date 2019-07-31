package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

public class ProductionSummaryAssert extends AbstractAssert<ProductionSummaryAssert, ProductionSummary> {

	private ProductionSummaryAssert(ProductionSummary actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static ProductionSummaryAssert assertThat(ProductionSummary productionSummary) {
		return new ProductionSummaryAssert(productionSummary, ProductionSummaryAssert.class);
	}

	public ProductionSummaryAssert hasMoreThenZero(String goodsTypeId) {
	    int amount = actual.getQuantity(goodsTypeId);
	    if (amount <= 0) {
	        failWithMessage("expect more then zero of <%s> goods type but has <%d>", goodsTypeId, amount);
	    }
	    return this;
	}
	
}
