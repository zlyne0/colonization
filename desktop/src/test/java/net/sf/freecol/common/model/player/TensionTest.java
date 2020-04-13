package net.sf.freecol.common.model.player;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

import net.sf.freecol.common.model.player.Tension.Level;

class TensionTest {

	@Test
	public void canDetermineWorstOrEqualsForContentLevel() throws Exception {
		// given
		Level content = Level.CONTENT;
		
		// when
		boolean actual = content.isWorstOrEquals(Level.CONTENT) && content.isWorstOrEquals(Level.HAPPY)
				&& !content.isWorstOrEquals(Level.DISPLEASED) && !content.isWorstOrEquals(Level.ANGRY);

		// then
		assertThat(actual).isTrue();
	}

	@Test
	public void canDetermineWorstOrEqualsForDispleasedLevel() throws Exception {
		// given
		Level displeased = Level.DISPLEASED;
		
		// when
		boolean actual = displeased.isWorstOrEquals(Level.CONTENT) && displeased.isWorstOrEquals(Level.HAPPY)
				&& displeased.isWorstOrEquals(Level.DISPLEASED) && !displeased.isWorstOrEquals(Level.ANGRY);

		// then
		assertThat(actual).isTrue();
	}
	
	
}
