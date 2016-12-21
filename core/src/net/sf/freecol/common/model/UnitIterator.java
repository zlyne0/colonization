package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sf.freecol.common.model.player.Player;

public class UnitIterator implements Iterator<Unit> {

	public static final Comparator<Unit> xyComparator = new Comparator<Unit>() {
		public int compare(Unit unit1, Unit unit2) {
			Tile tile1 = unit1.getTile();
			Tile tile2 = unit2.getTile();
			int cmp = ((tile1 == null) ? 0 : tile1.y)
					- ((tile2 == null) ? 0 : tile2.y);
			return (cmp != 0 || tile1 == null || tile2 == null) ? cmp
					: (tile1.x - tile2.x);
		}
	};
	
	
	private final Unit.UnitPredicate predicate;
	private final Player player;
	private final List<Unit> units = new ArrayList<Unit>();
	
	public UnitIterator(Player player, Unit.UnitPredicate predicate) {
		this.player = player;
		this.predicate = predicate;
		update();
	}
	
	private void update() {
		units.clear();
        for (Unit u : player.units.entities()) {
            if (predicate.obtains(u)) {
            	units.add(u);
            }
        }
        Collections.sort(units, xyComparator);
	}
	
	@Override
	public boolean hasNext() {
		while (!units.isEmpty()) {
			if (predicate.obtains(units.get(0))) {
				return true;
			}
			units.remove(0);
		}
		update();
		return !units.isEmpty();
	}

	@Override
	public Unit next() {
		if (!hasNext()) {
			throw new IllegalStateException("before next should call method hasNext to obtain");
		}
		return units.remove(0);
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("count: " + units.size());
		for (Unit u : units) {
			sb.append(", " + u.getId());
		}
		return sb.toString();
	}

    @Override
    public void remove() {
        throw new IllegalStateException("not implemented");
    }
}

