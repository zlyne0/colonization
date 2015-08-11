package promitech.colonization.gamelogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import promitech.colonization.GameLogic;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Unit;

/**
 * An <code>Iterator</code> of {@link Unit}s that can be made active.
 */
public class UnitIterator implements Iterator<Unit> {

    private final Player owner;

    private final Unit.UnitPredicate predicate;

    private final List<Unit> units = new ArrayList<Unit>();


    /**
     * Creates a new <code>UnitIterator</code>.
     *
     * @param owner The <code>Player</code> that needs an iterator of it's
     *            units.
     * @param predicate An object for deciding whether a <code>Unit</code>
     *            should be included in the <code>Iterator</code> or not.
     */
    public UnitIterator(Player owner, Unit.UnitPredicate predicate) {
        this.owner = owner;
        this.predicate = predicate;
        update();
    }


    /**
     * Update the internal units list with units that satisfy the
     * predicate.
     */
    private final void update() {
        units.clear();
        for (Unit u : owner.units.entities()) {
            if (predicate.obtains(u)) {
            	units.add(u);
            }
        }
        Collections.sort(units, GameLogic.xyComparator);
    }

    /**
     * Set the next valid unit.
     *
     * @param unit The <code>Unit</code> to put at the front of the list.
     * @return True if the operation succeeds.
     */
    public boolean setNext(Unit unit) {
        if (predicate.obtains(unit)) { // Of course, it has to be valid...
            Unit first = (units.isEmpty()) ? null : units.get(0);
            while (!units.isEmpty()) {
                if (units.get(0) == unit) return true;
                units.remove(0);
            }
            update();
            while (!units.isEmpty() && units.get(0) != first) {
                if (units.get(0) == unit) return true;
                units.remove(0);
            }
        }
        return false;
    }

    /**
     * Removes a specific unit from this unit iterator.
     *
     * @param u The <code>Unit</code> to remove.
     * @return True if the unit was removed.
     */
    public boolean remove(Unit u) {
        return units.remove(u);
    }

    /**
     * Reset the iterator.
     */
    public void reset() {
        update();
    }


    // Implement Iterator

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        // Try to find a unit that still satisfies the predicate.
        while (!units.isEmpty()) {
            if (predicate.obtains(units.get(0))) {
                return true; // Still valid
            }
            units.remove(0);
        }
        // Nothing left, so refill the units list.  If it is still
        // empty then there is definitely nothing left.
        update();
        return !units.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public Unit next() {
        return (hasNext()) ? units.remove(0) : null;
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
        next(); // Ignore value
    }
}
