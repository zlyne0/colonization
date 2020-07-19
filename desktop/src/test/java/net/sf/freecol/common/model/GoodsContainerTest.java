package net.sf.freecol.common.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class GoodsContainerTest {


	@Test
	public void canCalculateMaxGoodsAmountToFillFreeSlots() throws Exception {
		// given
		GoodsContainer sut = new GoodsContainer();

		// when
		int maxGoodsAmountToFillFreeSlots = sut.maxGoodsAmountToFillFreeSlots("one", 2);

		// then
		assertThat(maxGoodsAmountToFillFreeSlots).isEqualTo(200);
	}

	@Test
	public void canCalculateMaxGoodsAmountToFillFreeSlots2() throws Exception {
		// given
		GoodsContainer sut = new GoodsContainer();
		sut.increaseGoodsQuantity("one", 150);

		// when
		int maxGoodsAmountToFillFreeSlots = sut.maxGoodsAmountToFillFreeSlots("one", 2);

		// then
		assertThat(maxGoodsAmountToFillFreeSlots).isEqualTo(50);
	}

	@Test
	public void canCalculateMaxGoodsAmountToFillFreeSlots3() throws Exception {
		// given
		GoodsContainer sut = new GoodsContainer();
		sut.increaseGoodsQuantity("one", 20);
		sut.increaseGoodsQuantity("two", 50);

		// when
		int maxGoodsAmountToFillFreeSlots = sut.maxGoodsAmountToFillFreeSlots("one", 3);

		// then
		assertThat(maxGoodsAmountToFillFreeSlots).isEqualTo(180);
	}
	
	
}
