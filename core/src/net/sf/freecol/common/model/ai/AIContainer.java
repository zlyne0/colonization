package net.sf.freecol.common.model.ai;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class AIContainer {
	private final MapIdEntities<PlayerMissionsContainer> playerMissions = new MapIdEntities<PlayerMissionsContainer>();
	private final MapIdEntities<PlayerAiContainer> playersAiContainers = new MapIdEntities();

	public PlayerMissionsContainer getMissionContainer(String playerId) {
		return playerMissions.getByIdOrNull(playerId);
	}
	
	public PlayerMissionsContainer missionContainer(Player player) {
		PlayerMissionsContainer missionsContainer = playerMissions.getByIdOrNull(player.getId());
		if (missionsContainer == null) {
			missionsContainer = new PlayerMissionsContainer(player);
			playerMissions.add(missionsContainer);
		}
		return missionsContainer;
	}

	public PlayerAiContainer playerAiContainer(Player player) {
		PlayerAiContainer playerAiContainer = playersAiContainers.getByIdOrNull(player.getId());
		if (playerAiContainer == null) {
			playerAiContainer = new PlayerAiContainer(player);
			playersAiContainers.add(playerAiContainer);
		}
		return playerAiContainer;
	}

	public static class Xml extends XmlNodeParser<AIContainer> {

	    public Xml() {
	        addNodeForMapIdEntities("playerMissions", PlayerMissionsContainer.class);
	        addNodeForMapIdEntities("playersAiContainers", PlayerAiContainer.class);
	    }
	    
        @Override
        public void startElement(XmlNodeAttributes attr) {
            AIContainer aiContainer = new AIContainer();
            nodeObject = aiContainer;
        }

        @Override
        public String getTagName() {
            return tagName();
        }

        public static String tagName() {
            return "aiContainer";
        }
	    
	}
}
