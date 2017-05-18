package promitech.colonization;

import static org.junit.Assert.*;

import org.junit.Test;

public class SpiralIteratorTest {

	@Test
	public void shouldNotGenerateCoordinatesOutsideSize() throws Exception {
		// given
		
		SpiralIterator spiralIterator = new SpiralIterator(10, 10);
		spiralIterator.reset(8, 8, true, 5);
		
		// when
		while (spiralIterator.hasNext()) {
			int x = spiralIterator.getX();
			int y = spiralIterator.getY();
			
			// then
			assertTrue(x < 10);
			assertTrue(y < 10);
			
			spiralIterator.next();
		}
		
	}

}
