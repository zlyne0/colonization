package net.sf.freecol.common.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProductionSummaryTest {

	@Test
	void canCalculateMaxGoodsAmountToFillFreeSlots() {
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
	void canCalculateMaxGoodsAmountToFillFreeSlots2() {
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
	void canCalculateMaxGoodsAmountToFillFreeSlots3() {
		// given
		ProductionSummary sut = new ProductionSummary();
		sut.addGoods("two", 10);
		
		// when
		int maxGoodsAmountToFillFreeSlots = sut.maxGoodsAmountToFillFreeSlots("two", 2);

		// then
		assertThat(maxGoodsAmountToFillFreeSlots).isEqualTo(190);
	}

	@Test
	void canCalculateMaxGoodsAmountToFillFreeSlots4() throws Exception {
		// given
		ProductionSummary sut = new ProductionSummary();
		sut.addGoods("two", 10);
		
		// when
		int maxGoodsAmountToFillFreeSlots = sut.maxGoodsAmountToFillFreeSlots("one", 2);

		// then
		assertThat(maxGoodsAmountToFillFreeSlots).isEqualTo(100);
	}

	@Test
	void canCalculateMaxGoodsAmountToFillFreeSlots5() {
		// given
		ProductionSummary sut = new ProductionSummary();
		sut.addGoods("two", 10);
		sut.addGoods("three", 10);
		
		// when
		int maxGoodsAmountToFillFreeSlots = sut.maxGoodsAmountToFillFreeSlots("one", 3);

		// then
		assertThat(maxGoodsAmountToFillFreeSlots).isEqualTo(100);
	}

	@Test
	void canCalculateMaxGoodsAmountToFillFreeSlots6() {
		// given
		ProductionSummary sut = new ProductionSummary();
		sut.addGoods("two", 10);
		sut.addGoods("three", 10);

		// when
		int maxGoodsAmountToFillFreeSlots = sut.maxGoodsAmountToFillFreeSlots("two", 2);

		// then
		assertThat(maxGoodsAmountToFillFreeSlots).isEqualTo(90);
	}

	@Test
	void canCalculateMaxGoodsAmountToFillFreeSlots7() {
		// given
		ProductionSummary sut = new ProductionSummary();
		sut.addGoods("two", 110);
		sut.addGoods("three", 10);

		// when
		int maxGoodsAmountToFillFreeSlots = sut.maxGoodsAmountToFillFreeSlots("two", 3);

		// then
		assertThat(maxGoodsAmountToFillFreeSlots).isEqualTo(90);
	}

	@Test
	void canCalculateMaxGoodsAmountToFillFreeSlots8() {
		// given
		ProductionSummary sut = new ProductionSummary();
		sut.addGoods("two", 10);
		sut.addGoods("three", 10);

		// when
		int maxGoodsAmountToFillFreeSlots = sut.maxGoodsAmountToFillFreeSlots("two", 3);

		// then
		assertThat(maxGoodsAmountToFillFreeSlots).isEqualTo(190);
	}

	@Test
	void canVerifyHasPartOfGoods() {
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
	
    @Test
	void canVerifyHasPartOfGoodsForSingleGoods() {
        // given
        ProductionSummary sut = new ProductionSummary();
        sut.addGoods("one", 6);

        String reqType = "one";
        int reqAmount = 12;
        
        // when
        boolean hasPart_5 = sut.hasPart(reqType, reqAmount, 0.5f);
        boolean hasPart1 = sut.hasPart(reqType, reqAmount, 1f);

        // then
        assertThat(hasPart_5).isTrue();
        assertThat(hasPart1).isFalse();
    }
	
	@Test
    void canDecreaseToMinZero() {
        // given
        ProductionSummary sut = new ProductionSummary();
        sut.addGoods("one", 6);
        sut.addGoods("two", 3);

        ProductionSummary remove = new ProductionSummary();
        remove.addGoods("one", 5);
        remove.addGoods("two", 5);
        remove.addGoods("three", 5);
        
        // when
        sut.decreaseToMinZero(remove);

        // then
        ProductionSummaryAssert.assertThat(sut)
            .has("one", 1)
            .has("two", 0)
            .has("three", 0);
    }
}
