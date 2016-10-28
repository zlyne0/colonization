package net.sf.freecol.common.model.map.generator;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.cellular.CellularAutomataGenerator;

import promitech.colonization.SpiralIterator;

public class MyCellularAutomataGenerator extends CellularAutomataGenerator {

	private SpiralIterator spiralIterator;
	
	@Override
	public void generate(Grid grid) {
		spiralIterator = new SpiralIterator(grid.getWidth(), grid.getHeight());
		super.generate(grid);
	}
	
	@Override
	protected int countLivingNeighbors(Grid grid, int x, int y) {
		int count = 0;
		spiralIterator.reset(x, y, true, getRadius());
		while (spiralIterator.hasNext()) {
			if (isAlive(grid.get(spiralIterator.getX(), spiralIterator.getY()))) {
				count++;
			}
			spiralIterator.next();
		}
		return count;
	}
}
