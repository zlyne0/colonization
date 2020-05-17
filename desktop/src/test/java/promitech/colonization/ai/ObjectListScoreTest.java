package promitech.colonization.ai;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ObjectListScoreTest {

	@Test
	public void canSortDescending() throws Exception {
		// given
		ObjectsListScore<String> l = new ObjectsListScore<String>()
			.add("one", 10)
			.add("two", 20)
			.add("three", 30);
		
		// when
		l.sortDescending();

		// then
		assertThat(l.size()).isEqualTo(3);
		assertThat(l.scoreSum()).isEqualTo(60);
		assertThat(l.obj(0)).isEqualTo("three");
		assertThat(l.obj(1)).isEqualTo("two");
		assertThat(l.obj(2)).isEqualTo("one");
	}

}
