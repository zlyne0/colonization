package net.sf.freecol.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProductionSummaryTest {

	@Test
	public void canCalculateMaxGoodsAmountToFillFreeSlots() throws Exception {
		// given
		ProductionSummary sut = new ProductionSummary();
		sut.addGoods("one", 10);
		sut.addGoods("two", 10);
		
		// when
		int maxGoodsAmountToFillFreeSlots = sut.maxGoodsAmountToFillFreeSlots("three", 2);

		// then
		assertThat(maxGoodsAmountToFillFreeSlots).isEqualTo(0);
	}

	@Test
	public void canCalculateMaxGoodsAmountToFillFreeSlots2() throws Exception {
		// given
		ProductionSummary sut = new ProductionSummary();
		sut.addGoods("one", 10);
		sut.addGoods("two", 10);
		
		// when
		int maxGoodsAmountToFillFreeSlots = sut.maxGoodsAmountToFillFreeSlots("one", 2);

		// then
		assertThat(maxGoodsAmountToFillFreeSlots).isEqualTo(90);
	}

	@Test
	public void canCalculateMaxGoodsAmountToFillFreeSlots3() throws Exception {
		// given
		ProductionSummary sut = new ProductionSummary();
		sut.addGoods("two", 10);
		
		// when
		int maxGoodsAmountToFillFreeSlots = sut.maxGoodsAmountToFillFreeSlots("two", 2);

		// then
		assertThat(maxGoodsAmountToFillFreeSlots).isEqualTo(190);
	}
	
	
}
