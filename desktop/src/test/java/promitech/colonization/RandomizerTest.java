package promitech.colonization;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RandomizerTest {

	Randomizer sut = Randomizer.instance();
	
	@Test
	public void canGenerateNumberInBracket() {
		// given
		int min = 5;
		int max = 30;
		
		for (int i=0; i<1000; i++) {
			// when
			int r = sut.randomInt(min, max);
			
			// then
			assertTrue(r >= 5);
			assertTrue(r <= 30);
		}
	}

}
