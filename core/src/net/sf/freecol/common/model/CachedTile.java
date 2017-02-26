package net.sf.freecol.common.model;

import net.sf.freecol.common.model.player.Player;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class CachedTile {

	private Player player;
	
	public Player getPlayer() {
		return player;
	}

	public static class Xml extends XmlNodeParser<CachedTile> {

		private CachedTile cachedTileObject; 
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			if (cachedTileObject == null) {
				cachedTileObject = new CachedTile();
			}
			String playerId = attr.getStrAttribute("player");
			cachedTileObject.player = game.players.getById(playerId);
			
			nodeObject = cachedTileObject;
		}

		@Override
		public String getTagName() {
			return tagName();
		}
		
		public static String tagName() {
		    return "cachedTile";
		}
	}
}
