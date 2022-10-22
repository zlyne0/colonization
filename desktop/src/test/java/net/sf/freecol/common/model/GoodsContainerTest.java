package net.sf.freecol.common.model;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;

import net.sf.freecol.common.model.specification.AbstractGoods;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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

	@Test
	public void canCreateSlotedGoods1() throws Exception {
		// given
		GoodsContainer sut = new GoodsContainer();
		sut.increaseGoodsQuantity("tobacco", 50);
		sut.increaseGoodsQuantity("cloth", 30);

		// when
		List<AbstractGoods> slotedGoods = sut.slotedGoods();

		// then
		assertThat(slotedGoods)
			.hasSize(2)
			.extracting(AbstractGoods::getTypeId, AbstractGoods::getQuantity)
			.contains(
				Tuple.tuple("tobacco", 50), 
				Tuple.tuple("cloth", 30)
			)
		;
	}
	
	@Test
	public void canCreateSlotedGoods2() throws Exception {
		// given
		GoodsContainer sut = new GoodsContainer();
		sut.increaseGoodsQuantity("tobacco", 130);
		sut.increaseGoodsQuantity("cloth", 30);

		// when
		List<AbstractGoods> slotedGoods = sut.slotedGoods();

		// then
		assertThat(slotedGoods)
			.hasSize(3)
			.extracting(AbstractGoods::getTypeId, AbstractGoods::getQuantity)
			.contains(
				Tuple.tuple("tobacco", 100),
				Tuple.tuple("tobacco", 30),
				Tuple.tuple("cloth", 30)
			);
	}
}
