package net.sf.freecol.common.model.ai;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.player.Player;

public class AIContainer {
	private final MapIdEntities<PlayerMissionsContainer> playerMissions = new MapIdEntities<PlayerMissionsContainer>();

	public PlayerMissionsContainer getMissionContainer(Player player) {
		
		PlayerMissionsContainer missionsContainer = playerMissions.getByIdOrNull(player.getId());
		if (missionsContainer == null) {
			missionsContainer = new PlayerMissionsContainer(player);
			playerMissions.add(missionsContainer);
		}
		return missionsContainer;
	}
}
