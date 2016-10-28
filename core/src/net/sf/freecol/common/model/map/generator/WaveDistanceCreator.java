package net.sf.freecol.common.model.map.generator;

import com.badlogic.gdx.utils.IntArray;
import com.github.czyzby.noise4j.map.Grid;

import promitech.colonization.Direction;

public class WaveDistanceCreator {
	public static interface Initiator {
		public float val(int x, int y);
	}
	public static interface Consumer {
		public void val(int x, int y, float v);
	}
	
	static final float SRC = -1;
	static final float DEST = Float.MAX_VALUE;
	
	public final Grid grid;
	private int gx, gy;
	private float maxRange;
	private int cellIndex;
	
	public WaveDistanceCreator(int width, int height, float maxRange) {
		this.maxRange = maxRange;
		grid = new Grid(width, height);
	}
	
	public void init(WaveDistanceCreator.Initiator initiator) {
		for (cellIndex=0; cellIndex<grid.getArray().length; cellIndex++) {
			grid.getArray()[cellIndex] = initiator.val(grid.toX(cellIndex), grid.toY(cellIndex));
		}
	}
	
	public void generate() {
		IntArray poolIndex = new IntArray(false, grid.getWidth() * grid.getHeight());
		int x, y, nx, ny;
		float v, newValue;
		
		for (gy=0; gy<grid.getHeight(); gy++) {
			for (gx=0; gx<grid.getWidth(); gx++) {
				if (grid.get(gx, gy) != SRC) {
					continue;
				}

				newValue = 1;
				for (Direction d : Direction.longSides) {
					x = d.stepX(gx, gy);
					y = d.stepY(gx, gy);
					if (!grid.isIndexValid(x, y)) {
						continue;
					}
					v = grid.get(x, y);
					
					if (v != SRC) {
						if (newValue < v) {
							grid.set(x, y, newValue);
							poolIndex.add(grid.toIndex(x, y));
						}
					}
				}
				
				while (poolIndex.size != 0) {
					cellIndex = poolIndex.removeIndex(0);
					x = grid.toX(cellIndex);
					y = grid.toY(cellIndex);
					v = grid.getArray()[cellIndex];
					
					newValue = v + 1;
					for (Direction d : Direction.longSides) {
						nx = d.stepX(x, y);
						ny = d.stepY(x, y);
						if (!grid.isIndexValid(nx, ny)) {
							continue;
						}
						
						float av = grid.get(nx, ny);
						
						if (av != SRC && av > newValue && newValue <= maxRange) {
							grid.set(nx, ny, newValue);
							poolIndex.add(grid.toIndex(nx, ny));
						}
					}
				}
			}
		}
	}
	
	public void consume(WaveDistanceCreator.Consumer consumer) {
		for (cellIndex=0; cellIndex<grid.getArray().length; cellIndex++) {
			consumer.val(grid.toX(cellIndex), grid.toY(cellIndex), grid.getArray()[cellIndex]);
		}
		
	}
}