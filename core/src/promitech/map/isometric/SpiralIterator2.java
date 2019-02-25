package promitech.map.isometric;

import java.util.Iterator;
import java.util.NoSuchElementException;

import promitech.colonization.Direction;

class SpiralIterator2<TILE_TYPE> implements Iterator<TILE_TYPE> {
    public static final int UNDEFINED = Integer.MIN_VALUE;
    
    private IsometricMap<TILE_TYPE> map;
    
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
    
    public void reset(IsometricMap<TILE_TYPE> map, int centerX, int centerY, int radius) {
        this.map = map;
        this.radius = radius;
        
        boolean isFilled = true;
        
        n = 0;
        if (isFilled || radius == 1) {
            x = Direction.NE.stepX(centerX, centerY);
            y = Direction.NE.stepY(centerX, centerY);
            currentRadius = 1;
        } else {
            this.currentRadius = radius;
            x = centerX;
            y = centerY;
            for (int i = 1; i < radius; i++) {
                x = Direction.N.stepX(x, y);
                y = Direction.N.stepY(x, y);
            }
            x = Direction.NE.stepX(x, y);
            y = Direction.NE.stepY(x, y);
        }
        if (!map.isIndexValid(x, y)) {
            nextTile();
        }
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
        } while (!map.isIndexValid(x, y));
    }
    
    @Override
    public boolean hasNext() {
        return x != UNDEFINED && y != UNDEFINED;
    }

    @Override
    public TILE_TYPE next() {
        if (!hasNext()) {
            throw new NoSuchElementException("CircleIterator exhausted");
        }
        TILE_TYPE result = map.getSafeTile(x, y);
        nextTile();
        return result;
    }

	@Override
	public void remove() {
		throw new IllegalStateException("not implemented");
	}
    
}