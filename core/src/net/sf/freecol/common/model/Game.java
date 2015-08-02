package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;


public class Game {

	public Map map;
	public Player playingPlayer;
	public Specification specification;
	public final MapIdEntities<Player> players = new MapIdEntities<Player>();
	
	public String toString() {
		return "map[" + map + "], specification = " + specification;
	}
	
	
	public static class Xml extends XmlNodeParser {
		public Game game = new Game();
		
		public Xml() {
			super(null);
			
			addNode(new Specification.Xml(this));
			addNode(new Map.Xml(this));
			addNode(new Player.Xml(this));
			
			XmlNodeParser.game = game;
		}
		
		@Override
		public void startElement(String qName, Attributes attributes) {
		    System.out.println("static game");
		}
		
		@Override
		public String getTagName() {
			return "game";
		}
	}
	
}
