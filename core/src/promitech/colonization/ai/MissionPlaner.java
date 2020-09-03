package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;

public class MissionPlaner {

	private final Game game;
	
	private final NativeMissionPlaner nativeMissionPlaner;
	private final EuropeanMissionPlaner europeanMissionPlaner;

	public MissionPlaner(Game game, PathFinder pathFinder) {
		this.game = game;
		
		nativeMissionPlaner = new NativeMissionPlaner(pathFinder);
        europeanMissionPlaner = new EuropeanMissionPlaner(game, pathFinder);
	}
	
	public void planMissions(Player player) {
		if (player.isIndian()) {
			PlayerMissionsContainer playerMissionContainer = game.aiContainer.missionContainer(player);
			nativeMissionPlaner.prepareIndianWanderMissions(player, playerMissionContainer);
		}
		
		if (player.isLiveEuropeanPlayer()) {
			PlayerMissionsContainer playerMissionContainer = game.aiContainer.missionContainer(player);
			europeanMissionPlaner.prepareMissions(player, playerMissionContainer);
		}
	}
	
}
