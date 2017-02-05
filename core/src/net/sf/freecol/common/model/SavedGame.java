package net.sf.freecol.common.model;

import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class SavedGame implements Identifiable {
	public Game game;
	
	@Override
	public String getId() {
		throw new IllegalStateException("object without id");
	}
	
	public static class Xml extends XmlNodeParser<SavedGame> {
		public SavedGame savedGame = new SavedGame();

		public Xml() {
			addNode(Game.class, "game");
			nodeObject = savedGame;
		}
		
		@Override
		public void startElement(XmlNodeAttributes attr) {
		}

		@Override
		public String getTagName() {
			return tagName();
		}

		public static String tagName() {
			return "savedGame";
		}
		
	}

}
