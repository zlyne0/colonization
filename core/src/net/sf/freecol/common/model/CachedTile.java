package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class CachedTile implements Identifiable {

	private Player player;
	
	@Override
	public String getId() {
		throw new IllegalStateException("should not be invoked");
	}
	
	public Player getPlayer() {
		return player;
	}

	public static class Xml extends XmlNodeParser {

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
