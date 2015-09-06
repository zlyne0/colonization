package promitech.colonization.gamelogic;

import promitech.colonization.Direction;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;

public class MoveContext {
	public Direction direction;
	public MoveType moveType;
	public Unit unit;
	public Tile sourceTile;
	public Tile descTile;
}
