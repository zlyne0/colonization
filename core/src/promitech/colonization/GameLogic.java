package promitech.colonization;

import java.util.Comparator;

import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;

public class GameLogic {

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

}
