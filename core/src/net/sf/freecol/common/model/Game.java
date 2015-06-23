package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;


public class Game {

	public Map map;
	public Specification specification;
	
	public String toString() {
		return "map[" + map + "], specification = " + specification;
	}
	
	
	public static class Xml extends XmlNodeParser {
		public Game game = new Game();
		
		public Xml() {
			super(null);
			
			addNode(new Specification.Xml(this));
			addNode(new Map.Xml(this));
		}
		
		@Override
		public void startElement(String qName, Attributes attributes) {
		}
		
		@Override
		public String getTagName() {
			return "game";
		}
	}
	
}
