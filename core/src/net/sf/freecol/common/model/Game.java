package net.sf.freecol.common.model;

import org.xml.sax.Attributes;

import promitech.colonization.savegame.XmlNodeParser;


public class Game implements Identifiable {

    private String id;
    
	public Map map;
	public Player playingPlayer;
	public Specification specification;
	public final MapIdEntities<Player> players = new MapIdEntities<Player>();
	
	public String toString() {
		return "map[" + map + "], specification = " + specification;
	}

    @Override
    public String getId() {
        return id;
    }
	
	public static class Xml extends XmlNodeParser {
		public Game game = new Game();
		
		public Xml(Specification defaultSpecification) {
			super(null);
			addNode(new Specification.Xml(this));
			addNode(new Map.Xml(this));
			addNode(new MapIdEntities.Xml(this, "players", Player.class));
			
			game.specification = defaultSpecification;
			XmlNodeParser.game = game;
			this.nodeObject = game;
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
