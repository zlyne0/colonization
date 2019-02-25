package promitech.map.isometric;

import java.util.Iterator;

public class IterableSpiral<TT> implements Iterable<TT> {

	private SpiralIterator2<TT> spiralIterator;
	
	public IterableSpiral() {
		spiralIterator = new SpiralIterator2<TT>();
	}
	
	public void reset(IsometricMap<TT> map, int centerX, int centerY, int radius) {
		spiralIterator.reset(map, centerX, centerY, radius);
	}
	
	@Override
	public Iterator<TT> iterator() {
		return spiralIterator;
	}

}
