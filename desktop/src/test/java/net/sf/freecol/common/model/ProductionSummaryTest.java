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
	
	@Test
	public void canVerifyHasPartOfGoods() throws Exception {
		// given
		ProductionSummary sut = new ProductionSummary();
		sut.addGoods("one", 6);

		ProductionSummary req = new ProductionSummary();
		req.addGoods("one", 12);
		
		// when
		boolean hasPart_5 = sut.hasPart(req, 0.5f);
		boolean hasPart1 = sut.hasPart(req, 1f);

		// then
		assertThat(hasPart_5).isTrue();
		assertThat(hasPart1).isFalse();
	}
}
