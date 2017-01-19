package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GameLogic;

public class AILogic {

	private final Game game;
	private final GameLogic gameLogic;
	private final PathFinder pathFinder = new PathFinder();

	private final ExplorerMissionHandler explorerMissionHandler;
	
	public AILogic(Game game, GameLogic gameLogic, AIMoveDrawer aiMoveDrawer) {
		this.game = game;
		this.gameLogic = gameLogic;
		
		explorerMissionHandler = new ExplorerMissionHandler(game, pathFinder, aiMoveDrawer);
	}
	
	public void aiNewTurn(Player player) {
		gameLogic.newTurn(player);
		
		if (player.isIndian()) {
			return;
		}
		if (player.isLiveEuropeanPlayer()) {
			
			// create missions
			MapIdEntities<ExplorerMission> missions = new MapIdEntities<ExplorerMission>();
			for (Unit unit : player.units.entities()) {
				if (unit.isNaval()) {
					missions.add(new ExplorerMission(unit));
				}
			}

			// execute missions
			for (ExplorerMission explorerMission : missions.entities()) {
				explorerMissionHandler.executeMission(explorerMission);
			}
		}
	}
}
