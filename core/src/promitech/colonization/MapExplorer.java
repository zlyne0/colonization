package promitech.colonization;

import com.badlogic.gdx.utils.IntArray;
import com.github.czyzby.noise4j.array.Int2dArray;

import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.map.PathFinder;
import net.sf.freecol.common.model.player.Player;

class BorderCollector {
	public static final int UNSETED = -1;
	
	private final Int2dArray grid;
	
	public Map map;
	public Player player;
	public PathFinder pathFinder;
	
	// distances from highsea to unexplored tiles
	public IntArray borderIndexes = new IntArray();
	
	public BorderCollector(int w, int h) {
		grid = new Int2dArray(w, h);
		grid.set(UNSETED);
	}
	
	public int explorationCost(int x, int y) {
		return (100 - pathFinder.turnsCost(x, y)) * 1000 - pathFinder.totalCost(x, y) + grid.get(x, y) + numberOfLandNeighbour(x, y);
	}
	
	public void directionToExplore() {
		int x, y, v, cost;
		
		int theBestX = 0, theBestY = 0;
		int theBestCost = -1;
		
		for (int i=0; i<borderIndexes.size; i++) {
			x = grid.toX(borderIndexes.get(i));
			y = grid.toY(borderIndexes.get(i));
			v = grid.get(x, y);
			cost = explorationCost(x, y);
			if (cost >= theBestCost) {
				if (cost > theBestCost) {
					theBestCost = cost;
					theBestX = x;
					theBestY = y;
				} else {
					if (Randomizer.instance().isHappen(50)) {
						theBestCost = cost;
						theBestX = x;
						theBestY = y;
					}
				}
			}
		}
		Direction directionToExplore = null;
		if (theBestCost != -1) {
			directionToExplore = pathFinder.getDirectionInto(theBestX, theBestY);
			System.out.println("direction to explore = " + directionToExplore);
		}
		if (directionToExplore == null) {
			// maybe is everything explored or blocked some how
			System.out.println("WARING can not find path or direction to explore");
		}
	}
	
	public void showMax(String strings[][]) {
		System.out.println("BorderCollector. values on points");
		int x, y, v, cost;
		for (int i=0; i<borderIndexes.size; i++) {
			x = grid.toX(borderIndexes.get(i));
			y = grid.toY(borderIndexes.get(i));
			v = grid.get(x, y);
			cost = explorationCost(x, y);
			
			System.out.println("xy [" + x + "," + y + "] " + v +
					", turns\t" + pathFinder.turnsCost(x, y) +
					", totalC\t" + pathFinder.totalCost(x, y) +
					", v\t" + v +
					", land\t" + numberOfLandNeighbour(x, y) +
					" = cost " + cost);
			strings[y][x] = "" + cost;
		}
	}
	
	public int numberOfLandNeighbour(int x, int y) {
		int sum = 0;
		for (Direction d : Direction.allDirections) {
			Tile tile = map.getTile(x, y, d);
			if (tile.getType().isLand()) {
				sum++;
			}
		}
		return sum;
	}

	public boolean isTileUnexplored(int x, int y) {
		return !player.isTileExplored(x, y);
	}

	public void collect(int x, int y, int v) {
		if (v == 0) {
			return;
		}
		if (grid.get(x, y) == UNSETED) {
			borderIndexes.add(grid.toIndex(x, y));
		}
		grid.set(x, y, v);
	}
}

public class MapExplorer {
	
	public static interface Consumer {
		public void val(int x, int y, int v);
	}
	
	public static int SRC = 0;
	public static int DEST = Integer.MAX_VALUE;
	
	private final int maxRange;
	
	private final Int2dArray grid;
	private final IntArray poolIndexes;
	
	public final BorderCollector borderCollector;
	
	
	public MapExplorer(final int width, final int height) {
		this.maxRange = Integer.MAX_VALUE;
		
		grid = new Int2dArray(width, height);
		poolIndexes = new IntArray(false, grid.getWidth() * grid.getHeight());
		
		borderCollector = new BorderCollector(width, height);
		reset();
	}

	public void reset() {
		grid.set(DEST);
	}
	
	public void initializeSource(int x, int y) {
		poolIndexes.add(grid.toIndex(x, y));
		grid.set(x, y, SRC);
	}
	
	public void generate() {
		int cellIndex;
		int x, y, v;
		int nv;
		int dx, dy, dv;
		
		while (poolIndexes.size != 0) {
			cellIndex = poolIndexes.removeIndex(0);
			x = grid.toX(cellIndex);
			y = grid.toY(cellIndex);
			v = grid.get(x, y);
			
			nv = v + 1;
			for (Direction d : Direction.allDirections) {
				dx = d.stepX(x, y);
				dy = d.stepY(x, y);
				if (!grid.isIndexValid(dx, dy)) {
					continue;
				}
				if (isLand(dx, dy)) {
					continue;
				}
				dv = grid.get(dx, dy);
				
				if (dv != SRC && dv > nv && nv <= maxRange) {
					if (borderCollector.isTileUnexplored(dx, dy)) {
						borderCollector.collect(x, y, v);
					} else {
						grid.set(dx, dy, nv);
						poolIndexes.add(grid.toIndex(dx, dy));
					}
				}
			}
		}
	}

	private boolean isLand(int x, int y) {
		Tile tile = borderCollector.map.getSafeTile(x, y);
		return tile.getType().isLand();
	}

	public void consume(Consumer consumer) {
		int x, y, v;
		for (y=0; y<grid.getHeight(); y++) {
			for (x=0; x<grid.getWidth(); x++) {
				v = grid.get(x, y);
				consumer.val(x, y, v);
			}
		}
	}
}