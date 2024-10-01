package net.sf.freecol.common.model;

import net.sf.freecol.common.model.player.PlayerExploredTiles;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class CachedTile {

	private PlayerExploredTiles playerExploredTiles;
	private byte turn;

	public PlayerExploredTiles getPlayerExploredTiles() {
		return playerExploredTiles;
	}

	public byte getTurn() {
		return turn;
	}

	public static class Xml extends XmlNodeParser<CachedTile> {

		public static final String ATTR_PLAYER = "player";
		public static final String ATTR_TURN = "turn";
		public static final String ELEMENT_CACHED_TILE = "cachedTile";

		private CachedTile cachedTileObject; 
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
			if (cachedTileObject == null) {
				cachedTileObject = new CachedTile();
			}
			String playerId = attr.getStrAttribute(ATTR_PLAYER);
			cachedTileObject.playerExploredTiles = game.players.getById(playerId).getPlayerExploredTiles();
			cachedTileObject.turn = attr.getByteAttribute(ATTR_TURN, PlayerExploredTiles.FIRST);

			nodeObject = cachedTileObject;
		}

		@Override
		public String getTagName() {
			return tagName();
		}
		
		public static String tagName() {
		    return ELEMENT_CACHED_TILE;
		}
	}
}
