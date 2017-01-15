package promitech.colonization.ai;

import java.util.concurrent.Semaphore;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GUIGameController;
import promitech.colonization.GameLogic;
import promitech.colonization.gamelogic.MoveContext;

public class AILogic {

	private final GUIGameController guiGameController;
	private final Game game;
	private final GameLogic gameLogic;
	private final PathFinder pathFinder = new PathFinder();

	private final Semaphore animationSemaphore = new Semaphore(1);
	
	private final ExplorerMissionHandler explorerMissionHandler;
	
	public AILogic(Game game, GameLogic gameLogic, GUIGameController guiGameController) {
		this.game = game;
		this.gameLogic = gameLogic;
		this.guiGameController = guiGameController;
		
		explorerMissionHandler = new ExplorerMissionHandler(game, pathFinder, this);
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

	public void startAIUnitDislocationAnimation(MoveContext moveContext) {
		if (guiGameController.guiAIMoveInteraction(moveContext, this)) {
			// show unit move animation and waiting on it's end
			try {
				animationSemaphore.acquire(1);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	public void endOfAIUnitDislocationAnimation(MoveContext moveContext) {
		animationSemaphore.release(1);
	}
}
