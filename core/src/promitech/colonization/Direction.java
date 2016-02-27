package promitech.colonization;

import static net.sf.freecol.common.util.CollectionUtils.makeUnmodifiableList;

import java.util.List;

/**
 * The directions a Unit can move to. Includes deltas for moving
 * to adjacent squares, which are required due to the isometric
 * map. Starting north and going clockwise.
*/
public enum Direction {
    N  ( 0, -2,  0, -2),
    NE ( 1, -1,  0, -1),
    E  ( 1,  0,  1,  0),
    SE ( 1,  1,  0,  1),
    S  ( 0,  2,  0,  2),
    SW ( 0,  1, -1,  1),
    W  (-1,  0, -1,  0),
    NW ( 0, -1, -1, -1);

    public final static int NUMBER_OF_DIRECTIONS = values().length;

    public static final List<Direction> allDirections
        = makeUnmodifiableList(Direction.N, Direction.NE,
                               Direction.E, Direction.SE,
                               Direction.S, Direction.SW,
                               Direction.W, Direction.NW);

    public static final List<Direction> longSides
        = makeUnmodifiableList(Direction.NE, Direction.SE,
                               Direction.SW, Direction.NW);

    public static final List<Direction> corners
        = makeUnmodifiableList(Direction.N, Direction.E,
                               Direction.S, Direction.W);
    
    /** The direction increments. */
    private final int oddDX;
    private final int oddDY;
    private final int evenDX;
    private final int evenDY;


    /**
     * Create a new direction with the given increments.
     *
     * @param oddDX Delta X/odd.
     * @param oddDY Delta y/odd.
     * @param evenDX Delta X/even.
     * @param evenDY Delta y/even.
     */
    Direction(int oddDX, int oddDY, int evenDX, int evenDY) {
        this.oddDX = oddDX;
        this.oddDY = oddDY;
        this.evenDX = evenDX;
        this.evenDY = evenDY;
    }


    /**
     * Get the name key for this direction.
     *
     * @return The name key.
     */
    public String getNameKey() {
        return "direction." + this;
    }

    /**
     * Step an x coordinate in this direction.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @return The x coordinate after the step.
     */
    public int stepX(int x, int y) {
        return x + (((y & 1) != 0) ? oddDX : evenDX);
    }

    /**
     * Step a y coordinate in this direction.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @return The y coordinate after the step.
     */
    public int stepY(int x, int y) {
        return y + (((y & 1) != 0) ? oddDY : evenDY);
    }

    /**
     * Gets this direction rotated by n places.
     *
     * @param n The number of places to rotate
     *     (-#directions <= n <= #directions).
     * @return The rotated direction.
     */
    private Direction rotate(int n) {
        return values()[(ordinal() + n + NUMBER_OF_DIRECTIONS)
                        % NUMBER_OF_DIRECTIONS];
    }

    /**
     * Get the next direction after this one (clockwise).
     *
     * @return The next <code>Direction</code>.
     */
    public Direction getNextDirection() {
        return rotate(1);
    }

    /**
     * Get the previous direction after this one (anticlockwise).
     *
     * @return The previous <code>Direction</code>.
     */
    public Direction getPreviousDirection() {
        return rotate(-1);
    }

    /**
     * Gets the reverse direction of this one.
     *
     * @return The reverse <code>Direction</code>.
     */
    public Direction getReverseDirection() {
        return rotate(NUMBER_OF_DIRECTIONS/2);
    }

    /**
     * Convert an angle (radians) to a direction.
     *
     * @param angle The angle to convert.
     * @return An equivalent <code>Direction</code>.
     */
    public static Direction angleToDirection(double angle) {
        return Direction.values()[(int)Math.floor(angle / (Math.PI/4))];
    }
    
    public static Direction fromCoordinates(int x, int y, int x2, int y2) {
    	for (Direction d : Direction.values()) {
    		if (d.stepX(x, y) == x2 && d.stepY(x, y) == y2) {
    			return d;
    		}
    	}
    	return null;
    }
}
