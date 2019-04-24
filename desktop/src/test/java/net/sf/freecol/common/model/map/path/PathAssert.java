package net.sf.freecol.common.model.map.path;

import static org.junit.jupiter.api.Assertions.*;

import net.sf.freecol.common.model.Tile;

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
	
	public PathAssert lastStepEquals(int x, int y) {
		Tile tile = path.tiles.get(path.tiles.size-1);
		if (!tile.equalsCoordinates(x, y)) {
			fail("expected last tile in path at [" + x + ", " + y + "] but it is at [" + tile.x + ", " + tile.y + "]"); 
		}
		return this;
	}
	
	public PathAssert isEmpty() {
		if (path.tiles.size != 0) {
			fail("expected path is empty but has " + path.tiles.size + " nodes");
		}
		return this;
	}
}
