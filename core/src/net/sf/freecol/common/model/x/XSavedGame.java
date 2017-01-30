package net.sf.freecol.common.model.x;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Identifiable;
import net.sf.freecol.common.model.SavedGame;
import promitech.colonization.savegame.XmlNodeAttributes;
import promitech.colonization.savegame.XmlNodeParser;

public class XSavedGame implements Identifiable {

	public XGame game;
	
	@Override
	public String getId() {
		throw new IllegalStateException("object without id");
	}
	
	public static class Xml extends XmlNodeParser {
		public XSavedGame savedGame = new XSavedGame();

		public Xml() {
			addNode(XGame.class, "game");
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
