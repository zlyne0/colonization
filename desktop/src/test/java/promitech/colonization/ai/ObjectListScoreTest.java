package promitech.colonization.ai;

import org.junit.jupiter.api.Test;

class ObjectListScoreTest {

	@Test
	public void canSortDescending() throws Exception {
		// given
		ObjectsListScore<String> l = new ObjectsListScore<String>(3)
			.add("one", 10)
			.add("two", 20)
			.add("three", 30);
		
		// when
		l.sortDescending();

		// then
		ObjectsListScoreAssert.assertThat(l)
			.hasSize(3)
			.hasSumScore(60)
			.hasScore(0, 30, "three")
			.hasScore(1, 20, "two")
			.hasScore(2, 10, "one")
		;
	}

}
