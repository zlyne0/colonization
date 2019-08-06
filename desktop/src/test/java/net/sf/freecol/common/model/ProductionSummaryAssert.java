package net.sf.freecol.common.model;

import org.assertj.core.api.AbstractAssert;

public class ProductionSummaryAssert extends AbstractAssert<ProductionSummaryAssert, ProductionSummary> {

	private ProductionSummaryAssert(ProductionSummary actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static ProductionSummaryAssert assertThat(ProductionSummary productionSummary) {
		return new ProductionSummaryAssert(productionSummary, ProductionSummaryAssert.class);
	}

	public ProductionSummaryAssert hasNoLessThenZero(String goodsTypeId) {
	    int amount = actual.getQuantity(goodsTypeId);
	    if (amount < 0) {
	        failWithMessage("expect no less then zero of <%s> goods type but has <%d>", goodsTypeId, amount);
	    }
	    return this;
	}

    public ProductionSummaryAssert has(String goodsTypeId, int expectedAmount) {
        int amount = actual.getQuantity(goodsTypeId);
        if (amount != expectedAmount) {
            failWithMessage("expect <%d> of <%s> goods type but has <%d>", expectedAmount, goodsTypeId, amount);
        }
        return this;
    }
	
	
}
