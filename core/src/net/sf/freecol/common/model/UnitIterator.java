package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GameLogic;

public class UnitIterator implements Iterator<Unit> {

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
        Collections.sort(units, GameLogic.xyComparator);
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
}

