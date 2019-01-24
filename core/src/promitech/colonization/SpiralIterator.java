package promitech.colonization;

import java.util.Iterator;
import java.util.NoSuchElementException;

import promitech.map.AbstractArray2D;

/**
 * An iterator returning positions in a spiral starting at a given
 * center tile.  The center tile is never included in the returned
 * tiles, and all returned tiles are valid.
 */
public final class SpiralIterator extends AbstractArray2D implements Iterator<Boolean> {
	public static final int UNDEFINED = Integer.MIN_VALUE;
	
    /** The maximum radius. */
    private int radius;
    /** The current radius of the iteration. */
    private int currentRadius;
    /** The current index in the circle with the current radius: */
    private int n;
    /** The current position in the circle. */
    private int x, y;

	public SpiralIterator(int width, int height) {
		super(width, height);
	}

    public void reset(int centerX, int centerY, boolean isFilled, int radius) {
    	this.radius = radius;
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
    	if (!isCoordinateValid(x, y)) {
    	    nextTile();
    	}
    }
    
    private boolean isCoordinateValid(int px, int py) {
    	return px >= 0 && px < width && py >= 0 && py <height;
	}

	/**
     * Finds the next position.
     */
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

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return x != UNDEFINED && y != UNDEFINED;
    }

    public int getX() {
    	return x;
    }
    
    public int getY() {
    	return y;
    }

    public int getCoordsIndex() {
    	return toIndex(x, y);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean next() throws NoSuchElementException {
        if (!hasNext()) {
            throw new NoSuchElementException("CircleIterator exhausted");
        }
        nextTile();
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
