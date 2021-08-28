package promitech.colonization.ai.score;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

class ObjectScoreListTest {

	@Test
	public void canSortDescending() throws Exception {
		// given
		ObjectScoreList<String> l = new ObjectScoreList<String>(3)
			.add("one", 10)
			.add("two", 20)
			.add("three", 30);
		
		// when
		l.sortDescending();

		// then
		ScoreableObjectsListAssert.assertThat(l)
			.hasSize(3)
			.hasSumScore(60)
			.hasScore(0, 30, eq("three"))
			.hasScore(1, 20, eq("two"))
			.hasScore(2, 10, eq("one"))
		;
	}

	public static Predicate<String> eq(final String str) {
		return new Predicate<String>() {
			@Override
			public boolean test(String s) {
				return str.equals(s);
			}
		};
	}


}
