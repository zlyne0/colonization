package net.sf.freecol.common.model.map.path;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

public class PathAssert {

	private final Path path;
	private int stepIndex = 0;
	
	public static PathAssert assertThat(Path path) {
		return new PathAssert(path);
	}
	
	public PathAssert(Path path) {
		this.path = path;
	}
	
	public PathAssert assertPathStep(int stepIndex, int turns, int x, int y) {
	    assertEquals(turns, path.turns.get(stepIndex));
	    assertTrue(path.tiles.get(stepIndex).equalsCoordinates(x, y));
	    return this;
	}

	public PathAssert assertPathStep(int turns, int x, int y) {
	    assertEquals(turns, path.turns.get(stepIndex));
	    assertTrue(path.tiles.get(stepIndex).equalsCoordinates(x, y));
	    stepIndex++;
	    return this;
	}
	
}
