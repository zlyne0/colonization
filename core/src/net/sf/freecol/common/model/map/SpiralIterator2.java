package net.sf.freecol.common.model.map;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.Direction;

public class SpiralIterator2 implements Iterator<Tile> {
    public static final int UNDEFINED = Integer.MIN_VALUE;
    
    private Map map;
    private Tile center;
    
    /** The maximum radius. */
    private int radius;
    /** The current radius of the iteration. */
    private int currentRadius;
    /** The current index in the circle with the current radius: */
    private int n;
    /** The current position in the circle. */
    private int x, y;
    
    public SpiralIterator2() {
    }
    
    public void reset(Map map, Tile sourceTile, int radius) {
        this.map = map;
        this.center = sourceTile;
        this.radius = radius;
        
        boolean isFilled = true;
        
        n = 0;
        if (isFilled || radius == 1) {
            x = Direction.NE.stepX(center.x, center.y);
            y = Direction.NE.stepY(center.x, center.y);
            currentRadius = 1;
        } else {
            this.currentRadius = radius;
            x = center.x;
            y = center.y;
            for (int i = 1; i < radius; i++) {
                x = Direction.N.stepX(x, y);
                y = Direction.N.stepY(x, y);
            }
            x = Direction.NE.stepX(x, y);
            y = Direction.NE.stepY(x, y);
        }
        if (!isCoordinateValid(x, y)) {
            nextTile();
        }
    }
    
    private boolean isCoordinateValid(int px, int py) {
        return px >= 0 && px < map.width && py >= 0 && py < map.height;
    }
    
    private void nextTile() {
        boolean started = n != 0;
        do {
            n++;
            final int width = currentRadius * 2;
            if (n >= width * 4) {
                currentRadius++;
                if (currentRadius > radius) {
                    x = y = UNDEFINED;
                    break;
                } else if (!started) {
                    x = y = UNDEFINED;
                    break;
                } else {
                    n = 0;
                    started = false;
                    x = Direction.NE.stepX(x, y);
                    y = Direction.NE.stepY(x, y);
                }
            } else {
                int i = n / width;
                Direction direction;
                switch (i) {
                case 0:
                    direction = Direction.SE;
                    break;
                case 1:
                    direction = Direction.SW;
                    break;
                case 2:
                    direction = Direction.NW;
                    break;
                case 3:
                    direction = Direction.NE;
                    break;
                default:
                    throw new IllegalStateException("i=" + i + ", n=" + n + ", width=" + width);
                }
                x = direction.stepX(x, y);
                y = direction.stepY(x, y);
            }
        } while (!isCoordinateValid(x, y));
    }
    
    @Override
    public boolean hasNext() {
        return x != UNDEFINED && y != UNDEFINED;
    }

    @Override
    public Tile next() {
        if (!hasNext()) {
            throw new NoSuchElementException("CircleIterator exhausted");
        }
        Tile result = map.getSafeTile(x, y);
        nextTile();
        return result;
    }

	@Override
	public void remove() {
		throw new IllegalStateException("not implemented");
	}
    
}