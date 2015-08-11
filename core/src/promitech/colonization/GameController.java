package promitech.colonization;

import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Unit;
import promitech.colonization.actors.MapActor;
import promitech.colonization.gamelogic.UnitIterator;

public class GameController {

	private final MapActor mapActor;
	
	private final Player player;
	private final UnitIterator unitIterator;
	
	public GameController(Player player, MapActor mapActor) {
		this.mapActor = mapActor;
		this.player = player;
		
		unitIterator = new UnitIterator(player, new Unit.ActivePredicate());
	}

	public void nextActiveUnit() {
		if (unitIterator.hasNext()) {
			Unit nextUnit = unitIterator.next();
			mapActor.mapDrawModel().selectedUnit = nextUnit;
			if (nextUnit != null) {
				mapActor.centerCameraOnTile(nextUnit.getTile());
			} 
		}
	}
}
