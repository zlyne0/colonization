package promitech.colonization.math;

// orders of enum values is significant
public enum Directions {
    WEST(1, 0),
    NORTH_WEST(-1, 0),
    NORTH(0, -1),
    NORTH_EAST(0, -1),
    EAST(-1, 0),
    SOUTH_EAST(1, 0),
    SOUTH(0, 1),    
    SOUTH_WEST(0, 1);
    
    public final float vx;
    public final float vy;
    
    private Directions(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
    }
    
    public static final float A1 = 22.5f;
    public static final float A2 = 22.5f + 45;
    public static final float A3 = 22.5f + 90;
    public static final float A4 = 22.5f + 135;
    public static final float A_1 = -22.5f;
    public static final float A_2 = -22.5f - 45;
    public static final float A_3 = -22.5f - 90;
    public static final float A_4 = -22.5f - 135;
    public static final float A_5 = -22.5f - 135;
    
    public static Directions getDirectionByAngle(float angle) {
		Directions direction = null;
		if (angle >= A1 && angle < A2) {
			direction = Directions.NORTH_EAST;
		} else 
		if (angle >= A2 && angle < A3) {
			direction = Directions.NORTH;
		} else 
		if (angle >= A3 && angle < A4) {
			direction = Directions.NORTH_WEST;
		} else 
		if (angle >= A4 || angle < A_5) {
			direction = Directions.WEST;
		} else 
		if (angle >= A_1 && angle < A1) {
			direction = Directions.EAST;
		} else 
		if (angle >= A_2 && angle < A_1) {
			direction = Directions.SOUTH_EAST;
		} else
		if (angle >= A_3 && angle < A_2) {
			direction = Directions.SOUTH;
		} else
		if (angle >= A_4 && angle < A_3) {
			direction = Directions.SOUTH_WEST;
		} else
		if (direction == null) {
			throw new IllegalStateException("can not find direction for angle: " + angle);
		}
    	return direction;
    }
}
