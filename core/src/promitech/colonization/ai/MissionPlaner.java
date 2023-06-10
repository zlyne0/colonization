package promitech.colonization.ai;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.goodsToSell.TransportGoodsToSellMission;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;

public class MissionPlaner {

	private final Game game;

	private final NativeMissionPlaner nativeMissionPlaner;
	private final EuropeanMissionPlaner europeanMissionPlaner;
	private final MissionExecutor missionExecutor;

	public MissionPlaner(Game game, PathFinder pathFinder, MissionExecutor missionExecutor, PathFinder pathFinder2) {
		this.game = game;
		this.missionExecutor = missionExecutor;

		nativeMissionPlaner = new NativeMissionPlaner(game, pathFinder);
        europeanMissionPlaner = new EuropeanMissionPlaner(game, pathFinder, pathFinder2);
	}
	
	public void planMissions(Player player) {
		if (player.isIndian()) {
			PlayerMissionsContainer playerMissionContainer = game.aiContainer.missionContainer(player);
			nativeMissionPlaner.plan(player, playerMissionContainer);
		}
		
		if (player.isLiveEuropeanPlayer()) {
			PlayerMissionsContainer playerMissionContainer = game.aiContainer.missionContainer(player);

			// transport goods(sell) and then better plan mission
			missionExecutor.executeMissions(playerMissionContainer, TransportGoodsToSellMission.class);
			europeanMissionPlaner.prepareMissions(player, playerMissionContainer);
		}
	}
}