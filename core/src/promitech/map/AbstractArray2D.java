package promitech.map;

public abstract class AbstractArray2D {
    public final int width;
    public final int height;

    protected AbstractArray2D(final int width, final int height) {
        this.width = width;
        this.height = height;
    }
    
    public boolean isIndexValid(final int x, final int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    public int toIndex(final int x, final int y) {
        return x + y * width;
    }

    public int toX(final int index) {
        return index % width;
    }

    public int toY(final int index) {
        return index / width;
    }

    public boolean isSizeEquals(int w, int h) {
    	return width == w && height == h;
    }

    public boolean isSizeEquals(AbstractArray2D grid) {
        return this.width == grid.width && this.height == grid.height;
    }

}
