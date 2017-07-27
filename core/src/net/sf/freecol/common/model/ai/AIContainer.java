package net.sf.freecol.common.model.ai;

import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class AIContainer {
	public final MapIdEntities<PlayerMissionsContainer> playerMissions = new MapIdEntities<PlayerMissionsContainer>();

	public PlayerMissionsContainer getMissionContainer(Player player) {
		
		PlayerMissionsContainer missionsContainer = playerMissions.getByIdOrNull(player.getId());
		if (missionsContainer == null) {
			missionsContainer = new PlayerMissionsContainer(player);
			playerMissions.add(missionsContainer);
		}
		return missionsContainer;
	}
	
	public static class Xml extends XmlNodeParser<AIContainer> {

	    public Xml() {
	        addNodeForMapIdEntities("playerMissions", PlayerMissionsContainer.class);
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
