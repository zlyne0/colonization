package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.GameLogic;
import promitech.colonization.MoveLogic;

public class AILogic {

	private final Game game;
	private final GameLogic gameLogic;
	private final PathFinder pathFinder = new PathFinder();

	private final ExplorerMissionHandler explorerMissionHandler;
	private final WanderMissionHandler wanderMissionHandler;
	
	public AILogic(Game game, GameLogic gameLogic, MoveLogic moveLogic) {
		this.game = game;
		this.gameLogic = gameLogic;
		
		explorerMissionHandler = new ExplorerMissionHandler(game, pathFinder, moveLogic);
		wanderMissionHandler = new WanderMissionHandler(game, moveLogic);
	}
	
	public void aiNewTurn(Player player) {
		gameLogic.newTurn(player);
		
//		if (player.isIndian()) {
//			prepareIndianWanderMissions(player);
//			return;
//		}
		if (player.isLiveEuropeanPlayer()) {
			
			// create missions
			MapIdEntities<ExplorerMission> missions = new MapIdEntities<ExplorerMission>();
			for (Unit unit : player.units.entities()) {
				if (unit.isNaval() && unit.getTileLocationOrNull() != null) {
					missions.add(new ExplorerMission(unit));
				}
			}

			// execute missions
			for (ExplorerMission explorerMission : missions.entities()) {
				explorerMissionHandler.executeMission(explorerMission);
			}
		}
	}

	private void prepareIndianWanderMissions(Player player) {
		MapIdEntities<WanderMission> missions = new MapIdEntities<WanderMission>();
		
		for (Settlement settlement : player.settlements.entities()) {
			IndianSettlement tribe = (IndianSettlement)settlement;
			
			if (tribe.units.size() > tribe.settlementType.getMinimumSize() - 1) {
				for (int i=tribe.settlementType.getMinimumSize()-1; i<tribe.units.size(); i++) {
					missions.add(new WanderMission(tribe.units.get(i)));
				}
			}
		}
		
		wanderMissionHandler.executeMission(player, missions);
	}
}
